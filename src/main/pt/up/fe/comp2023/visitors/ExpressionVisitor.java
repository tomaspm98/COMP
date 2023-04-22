package pt.up.fe.comp2023.visitors;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.SymbolTable;
import pt.up.fe.comp2023.node.information.Method;
import pt.up.fe.comp2023.utils.ExpressionVisitorInformation;
import pt.up.fe.comp2023.utils.SymbolInfo;

import java.util.List;
import java.util.Optional;

public class ExpressionVisitor extends AJmmVisitor<String, ExpressionVisitorInformation> {

    private final SymbolTable symbolTable;
    private Integer currentAuxVariable;
    private Integer usedAuxVariables;

    public ExpressionVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.currentAuxVariable = 0;
        this.usedAuxVariables = 0;
    }

    public ExpressionVisitor(SymbolTable symbolTable, Integer startingAuxVariable) {
        this.symbolTable = symbolTable;
        this.currentAuxVariable = startingAuxVariable;
        this.usedAuxVariables = 0;
    }

    // Utility methods

    private String getNewAuxVariable() {
        String ret = "aux" + this.currentAuxVariable;
        this.currentAuxVariable++;
        this.usedAuxVariables++;
        return ret;
    }

    private ExpressionVisitorInformation visitExpressionAndStoreInfo(ExpressionVisitorInformation storage, JmmNode toVisit) {
        ExpressionVisitorInformation exprNodeInfo = visit(toVisit);
        storage.addAuxLines(exprNodeInfo.getAuxLines());
        return exprNodeInfo;
    }

    // End Utility methods
    @Override
    protected void buildVisitor() {

    }

    private ExpressionVisitorInformation dealWithMethodCall(JmmNode node, String methodName) {
        StringBuilder lastAuxLine = new StringBuilder();
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        String methodName = node.get("methodName");
        Optional<Method> optMethod = this.symbolTable.getMethodTry(methodName);

        if (optMethod.isEmpty()) {
            //TODO maybe add report
            System.err.println("Tried to search for method '" + methodName + "' but it wasn't found.");
            return ret;
        }

        Method method = optMethod.get();

        ExpressionVisitorInformation classNameInfo = visit(node.getJmmChild(0));
        ret.addAuxLines(classNameInfo.getAuxLines());

        String lastAuxVar = getNewAuxVariable();
        String methodType = OllirGenerator.jmmTypeToOllirType(method.getRetType());

        lastAuxLine.append(lastAuxVar).append(methodType)
                .append(" =.").append(methodType);
        lastAuxLine.append(" invokevirtual(")
                .append(classNameInfo.getResultNameAndType())
                .append(".")
                .append(classNameInfo.getOllirType())
                .append(", \"")
                .append(methodName)
                .append("\"");

        // Probably bad code but it probably works
        List<JmmNode> parameterExpressions = node.getChildren().subList(1, node.getNumChildren() - 1);
        for (JmmNode childNode : parameterExpressions) {
            ExpressionVisitorInformation paramExprInfo = visit(childNode);
            ret.addAuxLines(paramExprInfo.getAuxLines());
            lastAuxLine.append(", ").append(paramExprInfo.getResultNameAndType());
        }
        lastAuxLine.append(").").append(methodType);

        ret.addAuxLine(lastAuxLine.toString());
        ret.setOllirType(methodType);
        ret.setResultName(lastAuxVar);
        return ret;
    }

    /*
    arraylength($1.A.array.i32).i32
    ----
    arraylength(
    --
    ExprName.Type
    --
    )
    .i32
     */
    private ExpressionVisitorInformation dealWithArrayLength(JmmNode node, String methodName) {
        StringBuilder retName = new StringBuilder("arraylength(");

        //-- variable inside parenthesis

        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        JmmNode exprNode = node.getObject("array", JmmNode.class);

        var exprNodeInfo = visitExpressionAndStoreInfo(ret, exprNode);
        retName.append(exprNodeInfo.getResultNameAndType()).append(")");

        //-- return type

        ret.setOllirType("i32");
        return ret;
    }

    /*
    $1.A[i.i32].i32;
    ----
    ExprName
    --
    [
    --
    ExprName.Type
    --
    ]
    --
    .
    Type
     */
    private ExpressionVisitorInformation dealWithArrayAccess(JmmNode node, String methodName) {
        StringBuilder retName = new StringBuilder();
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        JmmNode arrayNode = node.getObject("array", JmmNode.class);
        JmmNode indexNode = node.getObject("index", JmmNode.class);

        ExpressionVisitorInformation arrayExprInfo = visitExpressionAndStoreInfo(ret, arrayNode);
        ExpressionVisitorInformation indexExprInfo = visitExpressionAndStoreInfo(ret, indexNode);

        //-- ExprName
        retName.append(arrayExprInfo.getResultName());

        //-- [ ExprName.Type ]
        retName.append("[").append(indexExprInfo.getResultNameAndType()).append("]");

        //-- return type
        SymbolInfo arrayVarInfo = symbolTable.getMostSpecificSymbol(methodName, arrayExprInfo.getResultName());
        if (arrayVarInfo == null) {
            System.err.println("arrayVarInfo is null! tried to search for '" + arrayExprInfo.getResultName() + "'.");
            return null;
        }

        //--
        ret.setResultName(retName.toString());
        ret.setOllirType(OllirGenerator.getArrayOllirType(arrayVarInfo.getSymbol().getType()));
        return ret;
    }

    private ExpressionVisitorInformation dealWithParenthesis(JmmNode node, String methodName) {
        JmmNode innerExprNode = node.getJmmChild(0);
        return visit(innerExprNode);
    }

    private ExpressionVisitorInformation dealWithUnaryBoolOp(JmmNode node, String methodName) { //TODO maybe this is not it chief
        StringBuilder retName = new StringBuilder("!.bool ");
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation(false);
        JmmNode expressionNode = node.getObject("bool", JmmNode.class);

        ExpressionVisitorInformation exprInfo = visitExpressionAndStoreInfo(ret, expressionNode);
        retName.append(exprInfo.getResultNameAndType());

        ret.setResultName(retName.toString());
        ret.setOllirType("bool");
        return ret;
    }

    private ExpressionVisitorInformation dealWithArithmeticBinaryOp(JmmNode node, String methodName) {
        StringBuilder retName = new StringBuilder();
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation(false);

        String opAndType = node.get(" op") + ".i32 ";

        JmmNode arg1Node = node.getObject("arg1", JmmNode.class);
        JmmNode arg2Node = node.getObject("arg2", JmmNode.class);

        ExpressionVisitorInformation arg1Info = visitExpressionAndStoreInfo(ret, arg1Node);
        ExpressionVisitorInformation arg2Info = visitExpressionAndStoreInfo(ret, arg2Node);

        retName.append(arg1Info.getResultNameAndType()).append(opAndType).append(arg2Info.getResultNameAndType());
        ret.setResultName(retName.toString());
        ret.setOllirType("i32");
        return ret;
    }

    private ExpressionVisitorInformation dealWithBoolBinaryOp(JmmNode node, String methodName) {
        StringBuilder retName = new StringBuilder();
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation(false);

        String opAndType = node.get(" op") + ".bool ";

        JmmNode arg1Node = node.getObject("arg1", JmmNode.class);
        JmmNode arg2Node = node.getObject("arg2", JmmNode.class);

        ExpressionVisitorInformation arg1Info = visitExpressionAndStoreInfo(ret, arg1Node);
        ExpressionVisitorInformation arg2Info = visitExpressionAndStoreInfo(ret, arg2Node);

        retName.append(arg1Info.getResultNameAndType()).append(opAndType).append(arg2Info.getResultNameAndType());
        ret.setResultName(retName.toString());
        ret.setOllirType("bool");
        return ret;
    }

    /*
    new int[A.length] -> new(array, t1.i32).array.i32
    new(array, expr.type).array.type;
     */
    private ExpressionVisitorInformation dealWithArrayInstantiation(JmmNode node, String methodName) {
        StringBuilder retName = new StringBuilder("new(array, ");
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        String ollirTypeName = OllirGenerator.jmmTypeToOllirType(node.get("typeName"));
        JmmNode sizeExpression = node.getObject("size", JmmNode.class);

        ExpressionVisitorInformation sizeInfo = visitExpressionAndStoreInfo(ret, sizeExpression);

        retName.append(sizeInfo.getResultNameAndType()).append(")");


        ret.setResultName(retName.toString());
        ret.setOllirType("array." + ollirTypeName);
        return ret;
    }

    private ExpressionVisitorInformation dealWithInstantiation(JmmNode node, String methodName) {
        StringBuilder retName = new StringBuilder("new(");
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        String ollirTypeName = OllirGenerator.jmmTypeToOllirType(node.get("name"));

        retName.append(ollirTypeName).append(")");

        ret.setResultName(retName.toString());
        ret.setOllirType(ollirTypeName);
        return ret;
    }

    private ExpressionVisitorInformation dealWithInteger(JmmNode node, String methodName) {
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        String value = node.get("value");
        ret.setResultName(value);
        ret.setOllirType("i32");
        return ret;
    }

    private ExpressionVisitorInformation dealWithBool(JmmNode node, String methodName) {
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        String value = node.get("value");
        ret.setResultName(value);
        ret.setOllirType("bool");
        return ret;
    }

    private ExpressionVisitorInformation dealWithID(JmmNode node, String methodName) {
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        String value = node.get("value");
        ret.setResultName(value);

        //TODO bruh

        ret.setOllirType("bool");
        return ret;
    }
}
