package pt.up.fe.comp2023.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.SymbolTable;
import pt.up.fe.comp2023.node.information.Method;
import pt.up.fe.comp2023.utils.ExpressionVisitorInformation;
import pt.up.fe.comp2023.utils.SymbolInfo;
import pt.up.fe.comp2023.utils.SymbolPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExpressionVisitor extends AJmmVisitor<String, ExpressionVisitorInformation> {

    private final SymbolTable symbolTable;
    private Integer currentAuxVariable;
    private Integer usedAuxVariables;

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

    private String getCurrentAuxVar() {
        return "aux" + (this.currentAuxVariable-1);
    }

    private ExpressionVisitorInformation visitExpressionAndStoreInfo(ExpressionVisitorInformation storage,
                                                                     JmmNode toVisit, String methodName) {
        ExpressionVisitorInformation exprNodeInfo = visit(toVisit, methodName);
        storage.addAuxLines(exprNodeInfo.getAuxLines());
        return exprNodeInfo;
    }

    private String getImportedMethodReturnType(JmmNode methodCallNode, String outerMethodName) {
        JmmNode parent = methodCallNode.getJmmParent();
        Optional<Method> optMethod = this.symbolTable.getMethodTry(outerMethodName);

        if (optMethod.isEmpty()) {
            System.err.println("Tried to find outer method '" + outerMethodName + "' in symbol table but it couldn't be found!");
            return "getImportedMethodReturnType error: outer method \"" + outerMethodName + "\" not found";
        }

        switch (parent.getKind()) {
            case "SimpleStatement" -> {
                return "V";
            }

            case "ClassFieldAssignment" -> {
                String fieldName = parent.getJmmChild(0).get("varName");
                for (var field : symbolTable.getFields()) {
                    if (field.getName().equals(fieldName))
                        return OllirGenerator.jmmTypeToOllirType(field.getType(), symbolTable.getClassName());
                }
                return "BIGERROR";
            }

            case "Assignment" -> {
                String varName = parent.get("varName");
                SymbolInfo symbolInfo = symbolTable.getMostSpecificSymbol(outerMethodName, varName);
                return OllirGenerator.jmmTypeToOllirType(symbolInfo.getSymbol().getType(), symbolTable.getClassName());
            }

            case "ArrayAssignment" -> {
                String varName = parent.get("varName");
                SymbolInfo symbolInfo = symbolTable.getMostSpecificSymbol(outerMethodName, varName);
                return OllirGenerator.getArrayOllirType(symbolInfo.getSymbol().getType(), symbolTable.getClassName());
            }

            case "MethodCall" -> {
                return getImportedMethodRootType(methodCallNode, outerMethodName);
            }

            case "ArrayAccess" -> {
                return "i32";
            }

            default -> {
                return "HUGEERROR";
            }
        }
    }

    public String getImportedMethodRootType(JmmNode exprStatement, String methodName) {
        JmmNode current = exprStatement;
        while (true) {
            JmmNode leftChild = current.getJmmChild(0);
            if (leftChild.getKind().equals("Identifier")) {
                Optional<SymbolInfo> symbolInfo = symbolTable.getMostSpecificSymbolTry(methodName, leftChild.get("value"));
                return symbolInfo.map(info -> OllirGenerator.jmmTypeToOllirType(info.getSymbol().getType(), "errorInGetImportedMethodRootType")).orElseGet(() -> leftChild.get("value"));
            }
            current = leftChild;
        }
    }

    /**
     * Given an Expression#MethodCall or Expression#ThisMethodCall, gets its children parameter nodes
     * and appends their name and type to line, comma separated. Starts off with a comma if at least one element is to
     * be appended.
     *
     * @param node       The JmmNode that has the parameter children. Should be an
     *                   Expression#MethodCall or Expression#ThisMethodCall
     * @param parentMethodName The method in which this node is in
     * @param ret        The callee's return variable, should be of type ExpressionVisitorInformation
     * @param line       The StringBuilder line to which the parameters will be appended to
     */
    private void getAndAppendParamsCommaSep(JmmNode node, String parentMethodName, ExpressionVisitorInformation ret,
                                            StringBuilder line) {
        int startingIndex = node.getKind().equals("MethodCall") ? 1 : 0;

        List<JmmNode> parameterExpressions =
                (node.getChildren().size() > startingIndex) ?
                        node.getChildren().subList(1, node.getNumChildren())
                        :
                        new ArrayList<>();

        for (JmmNode childNode : parameterExpressions) {
            ExpressionVisitorInformation paramExprInfo = visit(childNode, parentMethodName);
            ret.addAuxLines(paramExprInfo.getAuxLines());
            line.append(", ").append(paramExprInfo.getResultNameAndType());
        }
    }

    private boolean isDeclaredClassInstance(ExpressionVisitorInformation calledMethodsObj, String parentMethodName) {
        Optional<SymbolInfo> symbolInfoOpt = symbolTable.getMostSpecificSymbolTry(parentMethodName, calledMethodsObj.getResultName());

        if (symbolInfoOpt.isEmpty()) {
            return false;
        }

        SymbolInfo symbolInfo = symbolInfoOpt.get();
        return symbolInfo.getSymbol().getType().getName().equals(symbolTable.getClassName());
    }

    private boolean isImportedClass(ExpressionVisitorInformation calledMethodsObj) {
        return symbolTable.getImportedClasses().contains(calledMethodsObj.getResultName());
    }

    private boolean parentNodeIsSimpleStatement(JmmNode currentNode) {
        return currentNode.getJmmParent().getKind().equals("SimpleStatement");
    }

    // End Utility methods
    @Override
    protected void buildVisitor() {
        addVisit("MethodCall", this::dealWithGenericMethodCall);
        addVisit("ThisMethodCall", this::dealWithThisMethodCall);
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
    }

    private void dealWithDeclaredClassStaticMethodCall(JmmNode methodCallNode,
                                                       ExpressionVisitorInformation calledMethodsObj,
                                                       ExpressionVisitorInformation parentRet,
                                                       String parentMethodName) {

        String calledMethodName = methodCallNode.get("methodName");
        // Errors in this phase are NOT handled, should have been caught by semantic analysis
        Method calledMethod = symbolTable.getMethodOrWarn(calledMethodName, "dealWithThisMethodCall");
        String methodType = OllirGenerator.jmmTypeToOllirType(calledMethod.getRetType(), symbolTable.getClassName());

        if (methodType.equals("V") || parentNodeIsSimpleStatement(methodCallNode)) {
            StringBuilder returnLine = new StringBuilder();

            returnLine.append("invokestatic(this").append(", \"").append(calledMethod).append("\"");

            getAndAppendParamsCommaSep(methodCallNode, parentMethodName, parentRet, returnLine);
            returnLine.append(")");

            parentRet.setResultName(returnLine.toString());
            parentRet.setOllirType(methodType);
            return;
        }


        String methodCallHolderName = getNewAuxVariable();
        StringBuilder storeCallValueLine = new StringBuilder();
        storeCallValueLine.append(methodCallHolderName).append(".").append(methodType)
                .append(" :=.").append(methodType)
                .append(" invokestatic(this").append(", \"").append(calledMethod).append("\"");

        getAndAppendParamsCommaSep(methodCallNode, parentMethodName, parentRet, storeCallValueLine);
        storeCallValueLine.append(").").append(methodType).append(";");

        parentRet.addAuxLine(storeCallValueLine.toString());

        parentRet.setResultName(methodCallHolderName);
        parentRet.setOllirType(methodType);
    }

    // Will use invokevirtual(name.retType, "funcName");
    private void dealWithDeclaredClassInstanceMethodCall(JmmNode methodCallNode,
                                                         ExpressionVisitorInformation calledMethodsObj,
                                                         ExpressionVisitorInformation parentRet,
                                                         String parentMethodName) {

        String calledMethodName = methodCallNode.get("methodName");
        // Errors in this phase are NOT handled, should have been caught by semantic analysis
        Method calledMethod = symbolTable.getMethodOrWarn(calledMethodName, "dealWithThisMethodCall");
        String methodType = OllirGenerator.jmmTypeToOllirType(calledMethod.getRetType(), symbolTable.getClassName());

        if (methodType.equals("V") || parentNodeIsSimpleStatement(methodCallNode)) {
            StringBuilder returnLine = new StringBuilder();

            returnLine.append("invokevirtual(").append(calledMethodsObj.getResultNameAndType()).append(", \"").append(calledMethodName).append("\"");

            getAndAppendParamsCommaSep(methodCallNode, parentMethodName, parentRet, returnLine);
            returnLine.append(")");

            parentRet.setResultName(returnLine.toString());
            parentRet.setOllirType(methodType);
            return;
        }

        String methodCallHolderName = getNewAuxVariable();
        StringBuilder storeCallValueLine = new StringBuilder();
        storeCallValueLine.append(methodCallHolderName).append(".").append(methodType)
                .append(" :=.").append(methodType)
                .append(" invokevirtual(").append(calledMethodsObj.getResultNameAndType()).append(", \"").append(calledMethodName).append("\"");

        getAndAppendParamsCommaSep(methodCallNode, parentMethodName, parentRet, storeCallValueLine);
        storeCallValueLine.append(").").append(methodType).append(";");

        parentRet.addAuxLine(storeCallValueLine.toString());

        parentRet.setResultName(methodCallHolderName);
        parentRet.setOllirType(methodType);
    }

    private void dealWithImportedClassInstanceMethodCall(JmmNode methodCallNode,
                                                         ExpressionVisitorInformation calledMethodsObj,
                                                         ExpressionVisitorInformation parentRet,
                                                         String parentMethodName) {

        String calledMethodName = methodCallNode.get("methodName");

        String methodType = getImportedMethodReturnType(methodCallNode, parentMethodName);

        if (methodType.equals("V") || parentNodeIsSimpleStatement(methodCallNode)) {
            StringBuilder returnLine = new StringBuilder();

            returnLine.append("invokevirtual(").append(calledMethodsObj.getResultNameAndType()).append(", \"").append(calledMethodName).append("\"");

            getAndAppendParamsCommaSep(methodCallNode, parentMethodName, parentRet, returnLine);
            returnLine.append(")");

            parentRet.setResultName(returnLine.toString());
            parentRet.setOllirType(methodType);
            return;
        }

        StringBuilder params = new StringBuilder();
        getAndAppendParamsCommaSep(methodCallNode, parentMethodName, parentRet, params);
        String methodCallHolderName = getNewAuxVariable();

        String storeCallValueLine = methodCallHolderName + "." + methodType +
                " :=." + methodType +
                " invokevirtual(" + calledMethodsObj.getResultNameAndType() + ", \"" + calledMethodName + "\"" +
                params + ")." + methodType + ";";

        parentRet.addAuxLine(storeCallValueLine);

        parentRet.setResultName(methodCallHolderName);
        parentRet.setOllirType(methodType);
    }


    private void dealWithImportedClassStaticMethodCall(JmmNode methodCallNode,
                                                       ExpressionVisitorInformation calledMethodsObj,
                                                       ExpressionVisitorInformation parentRet,
                                                       String parentMethodName) {
        String calledMethodName = methodCallNode.get("methodName");

        String methodType = getImportedMethodReturnType(methodCallNode, parentMethodName);

        if (methodType.equals("V") || parentNodeIsSimpleStatement(methodCallNode)) {
            StringBuilder returnLine = new StringBuilder();

            returnLine.append("invokestatic(").append(calledMethodsObj.getResultName()).append(", \"").append(calledMethodName).append("\"");

            getAndAppendParamsCommaSep(methodCallNode, parentMethodName, parentRet, returnLine);
            returnLine.append(")");

            parentRet.setResultName(returnLine.toString());
            parentRet.setOllirType(methodType);
            return;
        }

        String methodCallHolderName = getNewAuxVariable();
        StringBuilder storeCallValueLine = new StringBuilder();
        storeCallValueLine.append(methodCallHolderName).append(".").append(methodType)
                .append(" :=.").append(methodType)
                .append(" invokestatic(").append(calledMethodsObj.getResultName()).append(", \"").append(calledMethodName).append("\"");

        getAndAppendParamsCommaSep(methodCallNode, parentMethodName, parentRet, storeCallValueLine);
        storeCallValueLine.append(").").append(methodType).append(";");

        parentRet.addAuxLine(storeCallValueLine.toString());

        parentRet.setResultName(methodCallHolderName);
        parentRet.setOllirType(methodType);
    }

    private ExpressionVisitorInformation dealWithGenericMethodCall(JmmNode methodCallNode, String parentMethodName) {
        ExpressionVisitorInformation evInfoRet = new ExpressionVisitorInformation();

        JmmNode calledMethodsObjNode = methodCallNode.getJmmChild(0);
        ExpressionVisitorInformation calledMethodsObj = visitExpressionAndStoreInfo(evInfoRet, calledMethodsObjNode, parentMethodName);

        if (calledMethodsObj.getResultName().equals(symbolTable.getClassName())) {
            dealWithDeclaredClassStaticMethodCall(methodCallNode, calledMethodsObj, evInfoRet, parentMethodName);
        }
        else if (isDeclaredClassInstance(calledMethodsObj, parentMethodName)) {
            dealWithDeclaredClassInstanceMethodCall(methodCallNode, calledMethodsObj, evInfoRet, parentMethodName);
        }
        else if (isImportedClass(calledMethodsObj)) {
            dealWithImportedClassStaticMethodCall(methodCallNode, calledMethodsObj, evInfoRet, parentMethodName);
        }
        else if (symbolTable.symbolIsDeclared(parentMethodName, calledMethodsObj.getResultName(), getCurrentAuxVar())) {
            dealWithImportedClassInstanceMethodCall(methodCallNode, calledMethodsObj, evInfoRet, parentMethodName);
        }
        else {
            System.err.println("dealWithGenericMethodCall could not determine which kind of method call this is.");
        }

        return evInfoRet;
    }

    private ExpressionVisitorInformation dealWithThisMethodCall(JmmNode methodCallNode, String parentMethodName) {
        ExpressionVisitorInformation retInfo = new ExpressionVisitorInformation();
        String calledMethodName = methodCallNode.get("methodName");

        // Errors in this phase are NOT handled, should have been caught by semantic analysis
        Method calledMethod = symbolTable.getMethodOrWarn(calledMethodName, "dealWithThisMethodCall");
        String methodType = OllirGenerator.jmmTypeToOllirType(calledMethod.getRetType(), symbolTable.getClassName());


        StringBuilder params = new StringBuilder();

        getAndAppendParamsCommaSep(methodCallNode, parentMethodName, retInfo, params);
        String methodCallHolderName = getNewAuxVariable();
        StringBuilder storeCallValueLine = new StringBuilder();

        storeCallValueLine.append(methodCallHolderName).append(".").append(methodType)
                .append(" :=.").append(methodType)
                .append(" invokevirtual(").append("this").append(", \"").append(calledMethod.getName()).append("\"");
        storeCallValueLine.append(params);
        storeCallValueLine.append(").").append(methodType).append(";");

        retInfo.addAuxLine(storeCallValueLine.toString());
        retInfo.setResultName(methodCallHolderName);

        retInfo.setOllirType(methodType);

        return retInfo;
    }

    private Boolean isNonStaticMethod() {

        return true;
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

        if (node.getJmmChild(0).getKind().equals("Identifier")) {
            String arrayName = node.getJmmChild(0).get("value");
            SymbolInfo arrayVarInfo = symbolTable.getMostSpecificSymbol(methodName, arrayExprInfo.getResultName());
            if (arrayVarInfo != null && arrayVarInfo.getSymbolPosition().equals(SymbolPosition.FIELD)) {
                String newAuxVar = getNewAuxVariable();
                StringBuilder getFieldAuxLine = new StringBuilder(newAuxVar);
                String arrayOllirType = OllirGenerator.jmmTypeToOllirType(arrayVarInfo.getSymbol().getType(), symbolTable.getClassName());
                getFieldAuxLine.append(".").append(arrayOllirType).append(" :=.").append(arrayOllirType).append(" getfield(").append("this").append(", ").append(arrayName).append(".").append(arrayOllirType).append(").").append(arrayOllirType).append(";");
                ret.addAuxLine(getFieldAuxLine.toString());
                retName.append(newAuxVar);
            }
            else {
                retName.append(arrayExprInfo.getResultName());
            }
        }
        else {
            retName.append(arrayExprInfo.getResultName());
        }

        //-- [ ExprName.Type ]
        String lastAuxVar = getNewAuxVariable();
        StringBuilder lastAuxLine = new StringBuilder(lastAuxVar);
        String arrayOllirType = arrayExprInfo.getOllirType();
        if (arrayOllirType.contains("array.")) {
            arrayOllirType = arrayOllirType.substring(arrayOllirType.indexOf("array.") + 6);
        }

        lastAuxLine.append(".").append(arrayOllirType).append(" :=.").append(arrayOllirType).append(" ").append(retName).append("[").append(indexExprInfo.getResultNameAndType()).append("].").append(arrayOllirType).append(";");
        ret.addAuxLine(lastAuxLine.toString());
        //--
        ret.setResultName(lastAuxVar);
        ret.setOllirType(arrayOllirType);
        return ret;
    }

    private ExpressionVisitorInformation dealWithParenthesis(JmmNode node, String methodName) {
        JmmNode innerExprNode = node.getJmmChild(0);
        return visit(innerExprNode, methodName);
    }

    private ExpressionVisitorInformation dealWithUnaryBoolOp(JmmNode node, String methodName) {
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
    }

    /*
    new int[A.length] -> new(array, t1.i32).array.i32
    new(array, expr.type).array.type;
     */
    private ExpressionVisitorInformation dealWithArrayInstantiation(JmmNode node, String methodName) {
        StringBuilder retName = new StringBuilder("new(array, ");
        ExpressionVisitorInformation ret = new ExpressionVisitorInformation();

        String ollirTypeName = OllirGenerator.jmmTypeToOllirType(node.get("typeName"), symbolTable.getClassName());
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

        String ollirTypeName = OllirGenerator.jmmTypeToOllirType(node.get("name"), symbolTable.getClassName());

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
            System.err.println("Tried to search for method '" + methodName + "' but it wasn't found.");
            return ret;
        }

        Method method = optMethod.get();


        Optional<SymbolInfo> symbolInfoOpt = this.symbolTable.getMostSpecificSymbolTry(methodName, value);

        if (symbolInfoOpt.isEmpty()) {
            ret.setResultName(value);
            ret.setOllirType(value);
            return ret;
        }

        SymbolInfo symbolInfo = symbolInfoOpt.get();

        switch (symbolInfo.getSymbolPosition()) {
            case LOCAL -> {
                ret.setResultName(symbolInfo.getSymbol().getName());
                ret.setOllirType(OllirGenerator.jmmTypeToOllirType(symbolInfo.getSymbol().getType(), symbolTable.getClassName()));
            }
            case PARAM -> {
                for (int i = 0; i < method.getArguments().size(); i++) {
                    Symbol param = method.getArguments().get(i);
                    if (param.getName().equals(value)) {
                        // removed $i.jmmVarName because apparently it's optional
                        ret.setResultName(value);
                        ret.setOllirType(OllirGenerator.jmmTypeToOllirType(param.getType(), symbolTable.getClassName()));
                    }
                }

            }
            case FIELD -> {
                for (Symbol field : symbolTable.getFields()) {
                    if (field.getName().equals(value)) {
                        String varAux = getNewAuxVariable();
                        String fieldType = OllirGenerator.jmmTypeToOllirType(symbolInfo.getSymbol().getType(), symbolTable.getClassName());
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
        ret.setOllirType(type);
        return ret;
    }

    public Integer getUsedAuxVariables() {
        return usedAuxVariables;
    }
}
