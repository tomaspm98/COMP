package pt.up.fe.comp2023.visitors;

import org.specs.comp.ollir.Ollir;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.SymbolTable;
import pt.up.fe.comp2023.node.information.Method;
import pt.up.fe.comp2023.utils.ExpressionVisitorInformation;
import pt.up.fe.comp2023.utils.SymbolInfo;

import java.util.ArrayList;
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

    private ExpressionVisitorInformation visitExpressionAndStoreInfo(ExpressionVisitorInformation storage, JmmNode toVisit, String methodName) {
        ExpressionVisitorInformation exprNodeInfo = visit(toVisit, methodName);
        storage.addAuxLines(exprNodeInfo.getAuxLines());
        return exprNodeInfo;
    }

    private String getImportedMethodReturnType(JmmNode methodCallNode, String outerMethodName) {
        JmmNode parent = methodCallNode.getJmmParent();
        ;
        Optional<Method> optMethod = this.symbolTable.getMethodTry(outerMethodName);

        if (optMethod.isEmpty()) {
            System.err.println("Tried to find method '" + outerMethodName + "' in symbol table but it couldn't be found!");
            return "MIDERROR";
        }

        Method outerMethod = optMethod.get();


        switch (parent.getKind()) {
            case "SimpleStatement" -> {
                return "V";
            }

            case "ClassFieldAssignment" -> {
                String fieldName = parent.getJmmChild(0).get("varName");
                for (var field : symbolTable.getFields()) {
                    if (field.getName().equals(fieldName)) return OllirGenerator.jmmTypeToOllirType(field.getType());
                }
                return "BIGERROR";
            }

            case "Assignment" -> {
                String varName = parent.getJmmChild(0).get("varName");
                SymbolInfo symbolInfo = symbolTable.getMostSpecificSymbol(outerMethodName, varName);
                return OllirGenerator.jmmTypeToOllirType(symbolInfo.getSymbol().getType());
            }

            case "ArrayAssignment" -> {
                String varName = parent.getJmmChild(0).get("varName");
                SymbolInfo symbolInfo = symbolTable.getMostSpecificSymbol(outerMethodName, varName);
                return OllirGenerator.getArrayOllirType(symbolInfo.getSymbol().getType());
            }

            default -> {
                return "HUGEERROR";
            }
        }
    }

    // End Utility methods
    @Override
    protected void buildVisitor() {
        addVisit("MethodCall", this::dealWithMethodCall);
        addVisit("ArrayLength", this::dealWithArrayLength);
        addVisit("ArrayAccess", this::dealWithArrayAccess);
        addVisit("Parenthesis", this::dealWithParenthesis);
        addVisit("UnaryBinaryOp", this::dealWithUnaryBoolOp);
        addVisit("ArithmeticBinaryOp", this::dealWithArithmeticBinaryOp);
        addVisit("BoolBinaryOp", this::dealWithBoolBinaryOp);
        addVisit("ArrayInstantiation", this::dealWithArrayInstantiation);
        addVisit("Instantiation", this::dealWithInstantiation);
        addVisit("Integer", this::dealWithInteger);
        addVisit("Boolean", this::dealWithBool);
        addVisit("Identifier", this::dealWithID);
        addVisit("ClassAccess", this::dealWithClassAccess);
        addVisit("ThisMethodCall", this::dealWithThisMethodCall);
    }

    private ExpressionVisitorInformation dealWithThisMethodCall(JmmNode node, String methodName) {
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        String className = "this";
        String calledMethod = node.get("methodName");
        Optional<Method> optMethod = this.symbolTable.getMethodTry(calledMethod);

        if (optMethod.isEmpty()) {
            System.err.println("Tried to access method '" + methodName + "' of this class, but it couldn't be found");
            return null;
        }

        Method method = optMethod.get();
        String methodType = OllirGenerator.jmmTypeToOllirType(method.getRetType());

        if (node.getJmmParent().getKind().equals("SimpleStatement")) {
            StringBuilder line = new StringBuilder();
            line.append("invokevirtual(")
                    .append("this")
                    .append(", \"")
                    .append(calledMethod)
                    .append("\"");

            List<JmmNode> parameterExpressions = (node.getChildren().size() > 1) ? node.getChildren().subList(1, node.getNumChildren()) : new ArrayList<>();
            for (JmmNode childNode : parameterExpressions) {
                ExpressionVisitorInformation paramExprInfo = visit(childNode, methodName);
                ret.addAuxLines(paramExprInfo.getAuxLines());
                line.append(", ").append(paramExprInfo.getResultNameAndType());
            }

            line.append(")");
            ret.setResultName(line.toString());
            ret.setOllirType(methodType);
        }

        StringBuilder lastAuxLine = new StringBuilder();


        String lastAuxVar = getNewAuxVariable();

        lastAuxLine.append(lastAuxVar).append(".").append(methodType)
                .append(" :=.").append(methodType);
        lastAuxLine.append(" invokevirtual(")
                .append("this")
                .append(", \"")
                .append(calledMethod)
                .append("\"");

        // Probably bad code but it probably works
        List<JmmNode> parameterExpressions = (node.getChildren().size() > 1) ? node.getChildren().subList(1, node.getNumChildren()) : new ArrayList<>();
        for (JmmNode childNode : parameterExpressions) {
            ExpressionVisitorInformation paramExprInfo = visit(childNode, methodName);
            ret.addAuxLines(paramExprInfo.getAuxLines());
            lastAuxLine.append(", ").append(paramExprInfo.getResultNameAndType());
        }
        lastAuxLine.append(").").append(methodType).append(";");

        ret.addAuxLine(lastAuxLine.toString());
        ret.setOllirType(methodType);
        ret.setResultName(lastAuxVar);
        return ret;


    }
    private ExpressionVisitorInformation dealWithMethodCall(JmmNode node, String methodName) {
        StringBuilder lastAuxLine = new StringBuilder();
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        String calledMethod = node.get("methodName");
        Optional<Method> optMethod = this.symbolTable.getMethodTry(calledMethod);


        if (optMethod.isEmpty()) {

            // I take back my previous comment, THIS is the worst code I've ever seen / written
            // avert thine eyes, lest thee seeketh the truth
            // TODO High priority refactor
            JmmNode classExprNode = node.getJmmChild(0);
            Optional<SymbolInfo> symbolInfo = symbolTable.getMostSpecificSymbolTry(methodName, classExprNode.get("value"));

            //call to a non static method of a variable of imported/unknown type
            if (classExprNode.getKind().equals("Identifier") && symbolInfo.isPresent() &&
                    !(symbolTable.getImportedClasses().stream().filter(
                            s -> s.equals(symbolInfo.get().getSymbol().getType().getName())
                    ).toList().isEmpty())
            ) {
                if (node.getJmmParent().getKind().equals("SimpleStatement")) {
                    StringBuilder line = new StringBuilder();
                    line.append("invokestatic(")
                            .append(OllirGenerator.jmmTypeToOllirType(symbolInfo.get().getSymbol().getType()))
                            .append(", \"")
                            .append(calledMethod)
                            .append("\"");

                    // Probably bad code but it probably works
                    List<JmmNode> parameterExpressions = (node.getChildren().size() > 1) ? node.getChildren().subList(1, node.getNumChildren()) : new ArrayList<>();
                    for (JmmNode childNode : parameterExpressions) {
                        ExpressionVisitorInformation paramExprInfo = visit(childNode, methodName);
                        ret.addAuxLines(paramExprInfo.getAuxLines());
                        line.append(", ").append(paramExprInfo.getResultNameAndType());
                    }
                    line.append(")");

                    ret.setOllirType("V");
                    ret.setResultName(line.toString());
                    return ret;
                }
                String lastAuxVar = getNewAuxVariable();
                String methodType = getImportedMethodReturnType(node, methodName);

                lastAuxLine.append(lastAuxVar).append(".").append(methodType)
                        .append(" :=.").append(methodType);
                lastAuxLine.append(" invokestatic(")
                        .append(classExprNode.get("value"))
                        .append(", \"")
                        .append(calledMethod)
                        .append("\"");

                // Probably bad code but it probably works
                List<JmmNode> parameterExpressions = (node.getChildren().size() > 1) ? node.getChildren().subList(1, node.getNumChildren()) : new ArrayList<>();
                for (JmmNode childNode : parameterExpressions) {
                    ExpressionVisitorInformation paramExprInfo = visit(childNode, methodName);
                    ret.addAuxLines(paramExprInfo.getAuxLines());
                    lastAuxLine.append(", ").append(paramExprInfo.getResultNameAndType());
                }
                lastAuxLine.append(").").append(methodType).append(";");

                ret.addAuxLine(lastAuxLine.toString());
                ret.setOllirType(methodType);
                ret.setResultName(lastAuxVar);
                return ret;
            }

            // call to a static method of an imported class
            else if (classExprNode.getKind().equals("Identifier") && !(symbolTable.getImportedClasses().stream().filter(
                    s -> s.equals(classExprNode.get("value"))
            ).toList().isEmpty())) {
                if (node.getJmmParent().getKind().equals("SimpleStatement")) {
                    StringBuilder line = new StringBuilder();
                    line.append("invokestatic(")
                            .append(classExprNode.get("value"))
                            .append(", \"")
                            .append(calledMethod)
                            .append("\"");

                    // Probably bad code but it probably works
                    List<JmmNode> parameterExpressions = (node.getChildren().size() > 1) ? node.getChildren().subList(1, node.getNumChildren()) : new ArrayList<>();
                    for (JmmNode childNode : parameterExpressions) {
                        ExpressionVisitorInformation paramExprInfo = visit(childNode, methodName);
                        ret.addAuxLines(paramExprInfo.getAuxLines());
                        line.append(", ").append(paramExprInfo.getResultNameAndType());
                    }
                    line.append(")");

                    ret.setOllirType("V");
                    ret.setResultName(line.toString());
                    return ret;
                }
                String lastAuxVar = getNewAuxVariable();
                String methodType = getImportedMethodReturnType(node, methodName);

                lastAuxLine.append(lastAuxVar).append(".").append(methodType)
                        .append(" :=.").append(methodType);
                lastAuxLine.append(" invokestatic(")
                        .append(classExprNode.get("value"))
                        .append(", \"")
                        .append(calledMethod)
                        .append("\"");

                // Probably bad code but it probably works
                List<JmmNode> parameterExpressions = (node.getChildren().size() > 1) ? node.getChildren().subList(1, node.getNumChildren()) : new ArrayList<>();
                for (JmmNode childNode : parameterExpressions) {
                    ExpressionVisitorInformation paramExprInfo = visit(childNode, methodName);
                    ret.addAuxLines(paramExprInfo.getAuxLines());
                    lastAuxLine.append(", ").append(paramExprInfo.getResultNameAndType());
                }
                lastAuxLine.append(").").append(methodType).append(";");

                ret.addAuxLine(lastAuxLine.toString());
                ret.setOllirType(methodType);
                ret.setResultName(lastAuxVar);
                return ret;
            }


            //TODO maybe add report
            System.err.println("Tried to search for method '" + calledMethod + "' but it wasn't found.");
            return ret;
        }

        Method method = optMethod.get();

        ExpressionVisitorInformation classNameInfo = visit(node.getJmmChild(0), methodName);
        ret.addAuxLines(classNameInfo.getAuxLines());

        String lastAuxVar = getNewAuxVariable();
        String methodType = OllirGenerator.jmmTypeToOllirType(method.getRetType());

        if (node.getJmmParent().getKind().equals("SimpleStatement")) { // No auxiliary variable needed
            StringBuilder line = new StringBuilder();
            line.append("invokevirtual(")
                    .append(classNameInfo.getResultNameAndType())
                    .append(", \"")
                    .append(calledMethod)
                    .append("\"");

            // Probably bad code but it probably works
            List<JmmNode> parameterExpressions = (node.getChildren().size() > 1) ? node.getChildren().subList(1, node.getNumChildren()) : new ArrayList<>();
            for (JmmNode childNode : parameterExpressions) {
                ExpressionVisitorInformation paramExprInfo = visit(childNode, methodName);
                ret.addAuxLines(paramExprInfo.getAuxLines());
                line.append(", ").append(paramExprInfo.getResultNameAndType());
            }
            line.append(")");
            ret.setOllirType("V");
            ret.setResultName(line.toString());
            return ret;
        }


        lastAuxLine.append(lastAuxVar).append(".").append(methodType)
                .append(" :=.").append(methodType);
        lastAuxLine.append(" invokevirtual(")
                .append(classNameInfo.getResultNameAndType())
                .append(", \"")
                .append(calledMethod)
                .append("\"");

        // Probably bad code but it probably works
        List<JmmNode> parameterExpressions = (node.getChildren().size() > 1) ? node.getChildren().subList(1, node.getNumChildren()) : new ArrayList<>();
        for (JmmNode childNode : parameterExpressions) {
            ExpressionVisitorInformation paramExprInfo = visit(childNode, methodName);
            ret.addAuxLines(paramExprInfo.getAuxLines());
            lastAuxLine.append(", ").append(paramExprInfo.getResultNameAndType());
        }
        lastAuxLine.append(").").append(methodType).append(";");

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

        var exprNodeInfo = visitExpressionAndStoreInfo(ret, exprNode, methodName);
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

        JmmNode arrayNode = node.getJmmChild(0);
        JmmNode indexNode = node.getJmmChild(1);

        ExpressionVisitorInformation arrayExprInfo = visitExpressionAndStoreInfo(ret, arrayNode, methodName);
        ExpressionVisitorInformation indexExprInfo = visitExpressionAndStoreInfo(ret, indexNode, methodName);

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
        return visit(innerExprNode, methodName);
    }

    private ExpressionVisitorInformation dealWithUnaryBoolOp(JmmNode node, String methodName) { //TODO maybe this is not it chief
        StringBuilder retName = new StringBuilder("!.bool ");
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation(false);
        JmmNode expressionNode = node.getObject("bool", JmmNode.class);

        ExpressionVisitorInformation exprInfo = visitExpressionAndStoreInfo(ret, expressionNode, methodName);
        retName.append(exprInfo.getResultNameAndType());

        ret.setResultName(retName.toString());
        ret.setOllirType("bool");
        return ret;
    }

    private ExpressionVisitorInformation dealWithArithmeticBinaryOp(JmmNode node, String methodName) {
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        JmmNode arg1Node = node.getJmmChild(0);
        JmmNode arg2Node = node.getJmmChild(1);
        String opAndType = node.get("op") + ".i32 ";

        ExpressionVisitorInformation arg1Info = visitExpressionAndStoreInfo(ret, arg1Node, methodName);
        ExpressionVisitorInformation arg2Info = visitExpressionAndStoreInfo(ret, arg2Node, methodName);

        String lastAuxVar = getNewAuxVariable();
        String type = "i32";
        String lastAuxLine = lastAuxVar + "." + type + " :=." + type + " " + arg1Info.getResultNameAndType() + " " + opAndType + arg2Info.getResultNameAndType() + ";";

        ret.addAuxLine(lastAuxLine);
        ret.setResultName(lastAuxVar);
        ret.setOllirType(type);

        return ret;


        //TODO in case we don't need to create an aux variable... might be useful in the future
        /*
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation(false);

        String opAndType = node.get(" op") + ".i32 ";

        JmmNode arg1Node = node.getObject("arg1", JmmNode.class);
        JmmNode arg2Node = node.getObject("arg2", JmmNode.class);

        ExpressionVisitorInformation arg1Info = visitExpressionAndStoreInfo(ret, arg1Node, methodName);
        ExpressionVisitorInformation arg2Info = visitExpressionAndStoreInfo(ret, arg2Node, methodName);

        retName.append(arg1Info.getResultNameAndType()).append(opAndType).append(arg2Info.getResultNameAndType());
        ret.setResultName(retName.toString());
        ret.setOllirType("i32");
        return ret;
        */

    }

    private ExpressionVisitorInformation dealWithBoolBinaryOp(JmmNode node, String methodName) {
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        JmmNode arg1Node = node.getJmmChild(0);
        JmmNode arg2Node = node.getJmmChild(1);
        String opAndType = node.get("op") + ".bool ";

        ExpressionVisitorInformation arg1Info = visitExpressionAndStoreInfo(ret, arg1Node, methodName);
        ExpressionVisitorInformation arg2Info = visitExpressionAndStoreInfo(ret, arg2Node, methodName);

        String lastAuxVar = getNewAuxVariable();
        String type = "bool";
        String lastAuxLine = lastAuxVar + "." + type + " :=." + type + " " + arg1Info.getResultNameAndType() + " " + opAndType + arg2Info.getResultNameAndType() + ";";

        ret.addAuxLine(lastAuxLine);
        ret.setResultName(lastAuxVar);
        ret.setOllirType(type);

        return ret;


        //TODO in case we don't need to create an aux variable... might be useful in the future
        /*
        StringBuilder retName = new StringBuilder();
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation(false);

        String opAndType = node.get(" op") + ".bool ";

        JmmNode arg1Node = node.getObject("arg1", JmmNode.class);
        JmmNode arg2Node = node.getObject("arg2", JmmNode.class);

        ExpressionVisitorInformation arg1Info = visitExpressionAndStoreInfo(ret, arg1Node, methodName);
        ExpressionVisitorInformation arg2Info = visitExpressionAndStoreInfo(ret, arg2Node, methodName);

        retName.append(arg1Info.getResultNameAndType()).append(opAndType).append(arg2Info.getResultNameAndType());
        ret.setResultName(retName.toString());
        ret.setOllirType("bool");
        return ret;
        */
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

        ExpressionVisitorInformation sizeInfo = visitExpressionAndStoreInfo(ret, sizeExpression, methodName);

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

        Optional<Method> optMethod = this.symbolTable.getMethodTry(methodName);

        if (optMethod.isEmpty()) {
            //TODO maybe add report
            System.err.println("Tried to search for method '" + methodName + "' but it wasn't found.");
            return ret;
        }

        Method method = optMethod.get();


        SymbolInfo symbolInfo = this.symbolTable.getMostSpecificSymbol(methodName, value);

        switch(symbolInfo.getSymbolPosition()) {
            case LOCAL -> {
                ret.setResultName(symbolInfo.getSymbol().getName());
                ret.setOllirType(OllirGenerator.jmmTypeToOllirType(symbolInfo.getSymbol().getType()));
            }
            case PARAM -> {
                for (int i = 0; i < method.getArguments().size(); i++) {
                    Symbol param = method.getArguments().get(i);
                    if (param.getName().equals(value)) {
                        ret.setResultName("$" + i + "." + value);
                        ret.setOllirType(OllirGenerator.jmmTypeToOllirType(param.getType()));
                    }
                }

            }
            case FIELD -> {
                for (Symbol field : symbolTable.getFields()) {
                    if (field.getName().equals(value)) {
                        String varAux = getNewAuxVariable();
                        String fieldType = OllirGenerator.jmmTypeToOllirType(symbolInfo.getSymbol().getType());
                        String auxLine = varAux + "." + fieldType + " :=." + fieldType + " getfield(this, " + field.getName() + "." + fieldType + ")." + fieldType + ";";
                        ret.addAuxLine(auxLine);
                        ret.setResultName(varAux);
                        ret.setOllirType(fieldType);
                    }
                }
            }
        }

        return ret;
    }

    private ExpressionVisitorInformation dealWithClassAccess(JmmNode node, String methodName) {
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();
        ret.setResultName("this");
        String type = symbolTable.getClassName();
        return ret;
    }

    public Integer getUsedAuxVariables() {
        return usedAuxVariables;
    }
}
