package pt.up.fe.comp2023.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.SymbolTable;
import pt.up.fe.comp2023.node.information.Method;
import pt.up.fe.comp2023.utils.ExpressionVisitorInformation;
import pt.up.fe.comp2023.utils.SymbolInfo;
import pt.up.fe.comp2023.utils.SymbolPosition;
import pt.up.fe.specs.util.collections.SpecsList;

import java.util.List;
import java.util.Optional;

public class OllirGenerator extends AJmmVisitor<String, String> {

    private static int indentation = 0;
    private final SymbolTable symbolTable;
    private final SpecsList<Report> reports;
    private int tempVariables;

    private int whileCounter;

    private int ifCounter;

    public OllirGenerator(SymbolTable symbolTable, SpecsList<Report> reports) {
        super();
        this.symbolTable = symbolTable;
        this.reports = reports;
        this.tempVariables = 0;
    }

    // Utility functions

    public static String jmmTypeToOllirType(String jmmType, String className, boolean isArray) {
        StringBuilder ret = new StringBuilder();
        if (isArray) {
            ret.append("array.");
        }
        switch (jmmType) {
            case "int" -> {
                ret.append("i32");
            }
            case "boolean" -> {
                ret.append("bool");
            }
            case "void" -> {
                ret.append("V");
            }
            case "this" -> {
                ret.append(className);
            }
            default -> {
                ret.append(jmmType);
            }
        }
        return ret.toString();
    }

    public static String jmmTypeToOllirType(Type jmmType, String className) {
        StringBuilder ret = new StringBuilder();
        if (jmmType.isArray()) {
            ret.append("array.");
        }
        return getString(jmmType, ret, className);
    }

    public static String getArrayOllirType(Type jmmType, String className) {
        StringBuilder ret = new StringBuilder();
        return getString(jmmType, ret, className);
    }

    private static String getString(Type jmmType, StringBuilder ret, String className) {
        switch (jmmType.getName()) {
            case "int" -> {
                ret.append("i32");
            }
            case "boolean" -> {
                ret.append("bool");
            }
            case "void" -> {
                ret.append("V");
            }
            case "this" -> {
                ret.append(className);
            }
            default -> {
                ret.append(jmmType.getName());
            }
        }
        return ret.toString();
    }

    public static String jmmSymbolToOllirSymbol(Symbol symbol, String className) {
        String name = symbol.getName();
        Type type = symbol.getType();

        StringBuilder ret = new StringBuilder(name).append(".");

        if (type.isArray()) {
            ret.append("array.");
        }

        ret.append(jmmTypeToOllirType(type, className));
        return ret.toString();
    }

    public static void increaseIdentation() {
        indentation++;
    }

    public static void decreaseIdentation() {
        indentation--;
    }

    public static String getIdentationString() {
        return "\t".repeat(indentation);
    }

    private String getNewIfTrueTag() {
        return "IFTRUE" + ifCounter;
    }

    private String getNewEndIfTag() {
        return "ENDIF" + ifCounter;
    }

    private String getNewWhileTag() {
        return "WHILE" + whileCounter;
    }

    private String getNewEndWhileTag() {
        return "ENDWHILE" + whileCounter;
    }

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("ImportDeclaration", this::dealWithImportDeclaration);
        addVisit("MethodDeclaration", this::dealWithMethodDeclaration);
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        addVisit("FieldDeclaration", this::dealWithFieldDeclaration);
        addVisit("Type", this::dealWithType);
        addVisit("ClassDeclaration", this::dealWithClassDeclaration);
        addVisit("Scope", this::dealWithScopeStatement);
        addVisit("Conditional", this::dealWithConditionalStatement);
        addVisit("Condition", this::dealWithCondition);
        addVisit("IfTrue", this::dealWithIfTrue);
        addVisit("ElseBlock", this::dealWithElseBlock);
        addVisit("SimpleStatement", this::dealWithSimpleStatement);
        addVisit("ClassFieldAssignment", this::dealWithClassFieldAssignmentStatement);
        addVisit("Assignment", this::dealWithAssignmentStatement);
        addVisit("ArrayAssignment", this::dealWithArrayAssignmentStatement);
    }

    private String jmmSymbolToOllirSymbol(JmmNode symbolNode) {
        StringBuilder ret = new StringBuilder();

        String fieldType = visit(symbolNode.getJmmChild(0));
        String fieldName = symbolNode.get("name");

        ret.append(fieldName).append(".").append(fieldType);
        return ret.toString();
    }

    private String getMethodName(JmmNode methodNode) {
        for (JmmNode child : methodNode.getChildren()) {
            if (methodNode.getKind().equals("Void") && child.getKind().equals("VoidMethodSymbol"))
                return child.get("name");
            else if (methodNode.getKind().equals("NonVoid") && child.getKind().equals("MethodSymbol")) return child.get("name");;
        }
        return null;
    }

    private String exprInfoToString(ExpressionVisitorInformation data) {
        StringBuilder ret = new StringBuilder();

        for (var auxLine : data.getAuxLines()) {
            ret.append(getIdentationString()).append(auxLine).append("\n");
        }

        ret.append(OllirGenerator.getIdentationString()).append(data.getResultNameAndType());
        return ret.toString();
    }

    private String exprAuxInfoToString(ExpressionVisitorInformation data) {
        StringBuilder ret = new StringBuilder();

        for (var auxLine : data.getAuxLines()) {
            ret.append(getIdentationString()).append(auxLine).append("\n");
        }

        return ret.toString();
    }

    public ExpressionVisitorInformation getOllirVariableNameAndType(Method method, String jmmVarName) {
        SymbolInfo symbolInfo = symbolTable.getMostSpecificSymbol(method.getName(), jmmVarName);
        ExpressionVisitorInformation evi = new ExpressionVisitorInformation();


        switch(symbolInfo.getSymbolPosition()) {
            case LOCAL -> {
                for (Symbol local : method.getVariables()) {
                    if (local.getName().equals(jmmVarName)) {
                        evi.setResultName(jmmVarName);
                        evi.setOllirType(OllirGenerator.jmmTypeToOllirType(local.getType(), symbolTable.getClassName()));
                    }
                }
            }
            case PARAM -> {
                for (int i = 0; i < method.getArguments().size(); i++) {
                    Symbol param = method.getArguments().get(i);
                    if (param.getName().equals(jmmVarName)) {
                        // removed $i.jmmVarName because apparently it's optional
                        evi.setResultName(jmmVarName);
                        evi.setOllirType(OllirGenerator.jmmTypeToOllirType(param.getType(), symbolTable.getClassName()));
                    }
                }

            }
            case FIELD -> {
                for (Symbol field : symbolTable.getFields()) {
                    if (field.getName().equals(jmmVarName)) {
                        String varAux = "aux" + tempVariables;
                        this.tempVariables++;
                        String fieldType = OllirGenerator.jmmTypeToOllirType(symbolInfo.getSymbol().getType(), symbolTable.getClassName());
                        String auxLine = varAux + "." + fieldType + " :=." + fieldType + " getfield(this, " + field.getName() + "." + fieldType + ")." + fieldType;
                        evi.addAuxLine(auxLine);
                        evi.setResultName(varAux);
                        evi.setOllirType(fieldType);
                    }
                }
            }
        }
        return evi;
    }


    // Visitors

    private String dealWithProgram(JmmNode node, String arg) {
        StringBuilder ret = new StringBuilder();
        for (JmmNode child : node.getChildren()) {
            if (child.getKind().equals("ClassDeclaration")) ret.append("\n");
            ret.append(visit(child)).append("\n");
        }
        return ret.toString();
    }

    private String dealWithImportDeclaration(JmmNode node, String __) {
        StringBuilder ret = new StringBuilder("import ");
        for (int i = 0; i < node.getNumChildren() - 1; i++) {
            String importFragment = node.getJmmChild(i).get("pathFragment");
            ret.append(importFragment).append(".");
        }
        String lastFragment = node.getJmmChild(node.getNumChildren() - 1).get("pathFragment");
        ret.append(lastFragment).append(";");
        return ret.toString();
    }

    private String dealWithClassDeclaration(JmmNode node, String __) {
        StringBuilder ret = new StringBuilder();

        String className = node.get("className");

        ret.append(className);

        Optional<String> superClassNameOpt = node.getOptional("superClassName");
        superClassNameOpt.ifPresent(s -> ret.append(" extends ").append(s));

        ret.append(" {\n\n");
        increaseIdentation();


        for (JmmNode fieldDeclaration : node.getChildren().stream().filter(s -> s.getKind().equals("FieldDeclaration")).toList()) {
            ret.append(visit(fieldDeclaration)).append("\n");
        }
        ret.append("\n");

        ret.append(getIdentationString()).append(".construct ").append(className).append("().V {\n");
        increaseIdentation();
        ret.append(getIdentationString()).append("invokespecial(this, \"<init>\").V;\n");
        decreaseIdentation();
        ret.append(getIdentationString()).append("}\n\n");

        for (JmmNode child : node.getChildren().stream().filter(s -> s.getHierarchy().contains("MethodDeclaration")).toList()) {
            ret.append(visit(child)).append("\n\n");
        }
        ret.append("}\n");
        decreaseIdentation();
        return ret.toString();
    }

    private String dealWithMethodDeclaration(JmmNode node, String __) {
        StringBuilder ret = new StringBuilder(getIdentationString() + ".method ");

        String methodName = this.getMethodName(node);
        Optional<Method> methodOp = this.symbolTable.getMethodTry(methodName);

        if (methodOp.isEmpty()) {
            System.err.println("Tried to get method with name '" + methodName + "' but it wasn't found in the symbol table");
            System.exit(1);
        }

        Method method = methodOp.get();

        for (int i = 0; i < method.getModifiers().size(); i++) {
            ret.append(method.getModifiers().get(i)).append(" ");
        }

        ret.append(methodName).append("(");

        for (int i = 0; i < method.getArguments().size(); i++) {
            Symbol argument = method.getArguments().get(i);

            ret.append(jmmSymbolToOllirSymbol(argument, symbolTable.getClassName()));

            if (i != method.getArguments().size() - 1) { // is not last element
                ret.append(", ");
            }
        }

        ret.append(").").append(jmmTypeToOllirType(method.getRetType(), symbolTable.getClassName())).append(" {\n");
        increaseIdentation();

        List<JmmNode> methodStatements = node.getChildren().stream().filter((child) -> child.getKind().equals("MethodStatement")).map((child) -> child.getJmmChild(0)) // get statement inside methodStatement
                .toList();

        for (JmmNode statement : methodStatements) {
            ret.append(visit(statement, methodName));
        }

        if (node.getKind().equals("NonVoid")) {
            JmmNode retExpressionNode = node.getChildren().get(node.getNumChildren() - 1).getJmmChild(0);
            ExpressionVisitor retExprVisitor = new ExpressionVisitor(this.symbolTable, this.tempVariables);
            ExpressionVisitorInformation retInfo = retExprVisitor.visit(retExpressionNode, methodName);
            this.tempVariables += retExprVisitor.getUsedAuxVariables();
            ret.append(exprAuxInfoToString(retInfo));
            ret.append(getIdentationString()).append("ret.");
            ret.append(retInfo.getOllirType()).append(" ").append(retInfo.getResultNameAndType()).append(";\n");
        }
        ret.append(getIdentationString()).append("}");
        decreaseIdentation();


        return ret.toString();
    }

    private String dealWithFieldDeclaration(JmmNode node, String __) {
        return getIdentationString() + ".field private " + jmmSymbolToOllirSymbol(node) + ";";
    }

    private String dealWithVarDeclaration(JmmNode node, String __) {
        return "";
    }

    private String dealWithType(JmmNode node, String __) {
        return jmmTypeToOllirType(node.get("typeName"), symbolTable.getClassName(), (boolean) node.getObject("isArray"));
    }

    private String dealWithScopeStatement(JmmNode node, String methodName) {
        StringBuilder ret = new StringBuilder();
        increaseIdentation();
        for (JmmNode child : node.getChildren()) {
            ret.append(visit(child, methodName)).append("\n");
        }
        decreaseIdentation();
        return ret.toString();
    }

    private String dealWithConditionalStatement(JmmNode node, String methodName) { //TODO add GOTOs
        StringBuilder ret = new StringBuilder();

        // BAD CODE !! If and While should have different types, but as to not interfere with other branches this is a patchwork solution
        if (node.getNumChildren() == 3) {
            return dealWithIfStatement(node, methodName);
        }
        return dealWithWhileStatement(node, methodName);

    }

    private String dealWithWhileStatement(JmmNode node, String methodName) {
        StringBuilder ret = new StringBuilder();
        String whileTag = getNewWhileTag();
        String endWhileTag = getNewEndWhileTag();
        whileCounter++;

        ret.append(getIdentationString()).append(whileTag).append(":\n");
        increaseIdentation();

        JmmNode conditionExpressionNode = node.getJmmChild(0).getJmmChild(0);
        ExpressionVisitor exprVisitor = new ExpressionVisitor(symbolTable, this.tempVariables);
        ExpressionVisitorInformation conditionInfo = exprVisitor.visit(conditionExpressionNode, methodName);
        this.tempVariables += exprVisitor.getUsedAuxVariables();

        for (var line : conditionInfo.getAuxLines()) {
            ret.append(getIdentationString()).append(line).append("\n");
        }

        ret.append(getIdentationString()).append("if (").append(conditionInfo.getResultNameAndType()).append(") goto ").append(endWhileTag).append(";\n");

        ret.append(dealWithElseBlock(node.getJmmChild(1), methodName));
        ret.append(getIdentationString()).append("goto ").append(whileTag).append(";\n");
        decreaseIdentation();
        ret.append(getIdentationString()).append(endWhileTag).append(":\n");

        return ret.toString();
    }

    private String dealWithIfStatement(JmmNode node, String methodName) {
        StringBuilder ret = new StringBuilder();

        JmmNode conditionExpressionNode = node.getJmmChild(0).getJmmChild(0);
        ExpressionVisitor exprVisitor = new ExpressionVisitor(symbolTable, this.tempVariables);
        ExpressionVisitorInformation conditionInfo = exprVisitor.visit(conditionExpressionNode, methodName);
        this.tempVariables += exprVisitor.getUsedAuxVariables();


        for (var line : conditionInfo.getAuxLines()) {
            ret.append(getIdentationString()).append(line).append("\n");
        }
        String ifTrueTag = getNewIfTrueTag();
        String endIfTag = getNewEndIfTag();
        ifCounter++;

        ret.append(getIdentationString()).append("if (").append(conditionInfo.getResultNameAndType()).append(") goto ").append(ifTrueTag).append(";\n");
        increaseIdentation();
        ret.append(dealWithElseBlock(node.getJmmChild(2), methodName));
        decreaseIdentation();
        ret.append(getIdentationString()).append("goto ").append(endIfTag).append(";\n");
        ret.append(getIdentationString()).append(ifTrueTag).append(":\n");
        increaseIdentation();
        ret.append(dealWithIfTrue(node.getJmmChild(1), methodName));
        ret.append(getIdentationString()).append("goto ").append(endIfTag).append(";\n");
        decreaseIdentation();
        ret.append(getIdentationString()).append(endIfTag).append(":\n");

        return ret.toString();
    }

    private String dealWithCondition(JmmNode node, String methodName) {
        ExpressionVisitor exprVisitor = new ExpressionVisitor(symbolTable, this.tempVariables);
        ExpressionVisitorInformation info = exprVisitor.visit(node.getJmmChild(0), methodName);
        this.tempVariables += exprVisitor.getUsedAuxVariables();
        return exprInfoToString(info);
    }

    private String dealWithIfTrue(JmmNode node, String methodName) {
        return visit(node.getJmmChild(0), methodName);
    }

    private String dealWithElseBlock(JmmNode node, String methodName) {
        return visit(node.getJmmChild(0), methodName);
    }

    private String dealWithSimpleStatement(JmmNode node, String methodName) {
        ExpressionVisitor exprVisitor = new ExpressionVisitor(symbolTable, this.tempVariables);
        JmmNode exprNode = node.getJmmChild(0);
        ExpressionVisitorInformation info = exprVisitor.visit(exprNode, methodName);
        this.tempVariables += exprVisitor.getUsedAuxVariables();

        return exprAuxInfoToString(info) + "\n\n";
    }

    private String dealWithClassFieldAssignmentStatement(JmmNode node, String methodName) {

        String fieldName = node.getJmmChild(0).get("varName");

        String fieldType = null;

        //TODO create method for this in symbolTable
        for (Symbol symbol : symbolTable.getFields()) {
            if (symbol.getName().equals(fieldName)) {
                fieldType = jmmTypeToOllirType(symbol.getType(), symbolTable.getClassName());
                break;
            }
        }
        if (fieldType == null) {
            System.err.println("Tried to find '" + fieldName + "'s type but failed");
            return null;
        }

        ExpressionVisitor exprVisitor = new ExpressionVisitor(symbolTable, this.tempVariables);
        ExpressionVisitorInformation info = exprVisitor.visit(node.getJmmChild(1), methodName);
        this.tempVariables += exprVisitor.getUsedAuxVariables();

        String lastLine = "";

        if (node.getJmmChild(1).getKind().equals("Instantiation")) {
            lastLine = "invokespecial(" + info.getResultNameAndType() +",\"<init>\").V;";
            info.addAuxLine(lastLine);
        }

        return exprAuxInfoToString(info) + getIdentationString() + "putfield(this, " + fieldName + "." + fieldType + ", " + info.getResultNameAndType() + ").V;\n\n";
    }

    // Please do not read this code. This is the worst code I have ever written.
    private String dealWithAssignmentStatement(JmmNode node, String methodName) {
        String varName = node.get("varName");

        Optional<SymbolInfo> symbolInfoOpt = symbolTable.getMostSpecificSymbolTry(methodName, varName);

        JmmNode assignedExprNode =  node.getJmmChild(0);

        if (symbolInfoOpt.isPresent() && symbolInfoOpt.get().getSymbolPosition().equals(SymbolPosition.FIELD)) {
            ExpressionVisitor expressionVisitor = new ExpressionVisitor(this.symbolTable, this.tempVariables);
            ExpressionVisitorInformation assignedExprNodeData = expressionVisitor.visit(assignedExprNode, methodName);
            this.tempVariables += expressionVisitor.getUsedAuxVariables();
            return exprAuxInfoToString(assignedExprNodeData) + getIdentationString() + "putfield(this, " + jmmSymbolToOllirSymbol(symbolInfoOpt.get().getSymbol(), symbolTable.getClassName()) + ", " + assignedExprNodeData.getResultNameAndType() + ").V;\n\n";
        }

        Optional<Method> optMethod = this.symbolTable.getMethodTry(methodName);

        if (optMethod.isEmpty()) {
            //TODO maybe add report
            System.err.println("Tried to search for method '" + methodName + "' but it wasn't found.");
            return null;
        }

        Method method = optMethod.get();

        ExpressionVisitorInformation leftVarData = getOllirVariableNameAndType(method, varName);

        ExpressionVisitor expressionVisitor = new ExpressionVisitor(this.symbolTable, this.tempVariables);
        ExpressionVisitorInformation assignedExprNodeData = expressionVisitor.visit(assignedExprNode, methodName);
        this.tempVariables += expressionVisitor.getUsedAuxVariables();

        String lastLine = "\n";
        if (node.getJmmChild(0).getKind().equals("Instantiation")) {
            lastLine = getIdentationString() + "invokespecial(" + varName + "." + assignedExprNodeData.getOllirType() + ",\"<init>\").V;\n\n";
        }

        return exprAuxInfoToString(leftVarData) + exprAuxInfoToString(assignedExprNodeData) + getIdentationString() + varName + "." + assignedExprNodeData.getOllirType()
                + " :=." + assignedExprNodeData.getOllirType() + " " + assignedExprNodeData.getResultNameAndType() + ";\n" + lastLine;
    }

    private String dealWithArrayAssignmentStatement(JmmNode node, String methodName) {
        String varName = node.get("varName");

        SymbolInfo arrayVarInfo = symbolTable.getMostSpecificSymbol(methodName, varName);
        if (arrayVarInfo == null) {
            System.err.println("arrayVarInfo is null! tried to search for '" + varName + "'.");
            return null;
        }


        JmmNode arrayIndexExpression =  node.getJmmChild(0).getJmmChild(0);
        JmmNode assignedExpression =  node.getJmmChild(1);

        ExpressionVisitor indexExpressionVisitor = new ExpressionVisitor(this.symbolTable, this.tempVariables);
        ExpressionVisitorInformation indexExpressionData = indexExpressionVisitor.visit(arrayIndexExpression, methodName);
        this.tempVariables += indexExpressionVisitor.getUsedAuxVariables();

        ExpressionVisitor assignedExpressionVisitor = new ExpressionVisitor(this.symbolTable, this.tempVariables);
        ExpressionVisitorInformation assignedExpressionData = assignedExpressionVisitor.visit(assignedExpression, methodName);
        this.tempVariables += assignedExpressionVisitor.getUsedAuxVariables();

        String lastLine = "\n";

        /*
        will contain something like
            aux0 = getfield(this, a.array.i32);
            aux0

        or
            a
         in case it's a local/argument variable.
         */
        StringBuilder arrayHolderAndAuxLines = new StringBuilder();
        String varOllirType = jmmTypeToOllirType(arrayVarInfo.getSymbol().getType(), symbolTable.getClassName());

        if (arrayVarInfo.getSymbolPosition().equals(SymbolPosition.FIELD)) {
            String newAuxVarName = "aux" + tempVariables;
            tempVariables++;
            arrayHolderAndAuxLines.append(getIdentationString()).append(newAuxVarName).append(".").append(varOllirType).append(" :=.").append(varOllirType).append(" getfield(this, ").append(varName).append(".").append(varOllirType).append(").").append(varOllirType).append(";\n")
                    .append(getIdentationString()).append(newAuxVarName);
        } else {
            arrayHolderAndAuxLines.append(getIdentationString()).append(varName);
        }

        if (node.getJmmChild(1).getKind().equals("Instantiation")) {
            lastLine = getIdentationString() + "invokespecial(" + varName + "[" + indexExpressionData.getResultNameAndType() + "]." + getArrayOllirType(arrayVarInfo.getSymbol().getType(), symbolTable.getClassName())
                    + ",\"<init>\").V;\n\n";
        }

        return exprAuxInfoToString(indexExpressionData) +
                exprAuxInfoToString(assignedExpressionData) +
                arrayHolderAndAuxLines + "[" + indexExpressionData.getResultNameAndType() + "]." + getArrayOllirType(arrayVarInfo.getSymbol().getType(), symbolTable.getClassName())
                +  " :=." + assignedExpressionData.getOllirType() + " " + assignedExpressionData.getResultNameAndType() + ";\n" + lastLine;
    }

    /* Probably uneeded
    private String dealWithIntExpression(JmmNode node, String methodName) {
        ExpressionVisitor expressionVisitor = new ExpressionVisitor(this.symbolTable, this.tempVariables);
        ExpressionVisitorInformation exprInfo = expressionVisitor.visit(node, methodName);
        this.tempVariables += expressionVisitor.getUsedAuxVariables();
        return exprInfoToString(exprInfo);
    }
    */
}
