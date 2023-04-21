package pt.up.fe.comp2023.visitors;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.SymbolTable;
import pt.up.fe.comp2023.node.information.Method;
import pt.up.fe.comp2023.utils.ExpressionVisitorInformation;

import java.util.List;
import java.util.Optional;

public class ExpressionVisitor extends AJmmVisitor<String, ExpressionVisitorInformation> {

    private SymbolTable symbolTable;
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
        List<JmmNode> parameterExpressions = node.getChildren().subList(1, node.getNumChildren()-1);
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

        //--
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        JmmNode exprNode = node.getObject("array", JmmNode.class);

        var exprNodeInfo = visitExpressionAndStoreInfo(ret, exprNode);
        retName.append(exprNodeInfo.getResultNameAndType());

        //--

        retName.append(")");
        ret.setResultName(retName.toString());

        String retType = "i32";
        ret.setOllirType(retType);
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

        ExpressionVisitorInformation arrayExprInfo = visitExpressionAndStoreInfo(ret, arrayNode);;
        ExpressionVisitorInformation indexExprInfo = visitExpressionAndStoreInfo(ret, indexNode);

        ret.setOllirType();

        ret.setResultName(retName.toString());
        return ret;

    }

    }
