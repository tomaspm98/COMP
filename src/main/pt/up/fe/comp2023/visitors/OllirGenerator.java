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

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public class OllirGenerator extends AJmmVisitor<String, String> {

    private static int identantion = 0;
    private final SymbolTable symbolTable;
    private final SpecsList<Report> reports;
    private int tempVariables;

    public OllirGenerator(SymbolTable symbolTable, SpecsList<Report> reports) {
        super();
        this.symbolTable = symbolTable;
        this.reports = reports;
        this.tempVariables = 0;
    }

    // Utility functions

    public static String jmmTypeToOllirType(String jmmType) {
        switch (jmmType) {
            case "int" -> {
                return "i32";
            }
            case "boolean" -> {
                return "bool";
            }
            case "void" -> {
                return "V";
            }
            default -> {
                return jmmType;
            }
        }
    }

    public static String jmmTypeToOllirType(Type jmmType) {
        StringBuilder ret = new StringBuilder();
        if (jmmType.isArray()) {
            ret.append("array.");
        }
        return getString(jmmType, ret);
    }

    public static String getArrayOllirType(Type jmmType) {
        StringBuilder ret = new StringBuilder();
        return getString(jmmType, ret);
    }

    private static String getString(Type jmmType, StringBuilder ret) {
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
            default -> {
                ret.append(jmmType.getName());
            }
        }
        return ret.toString();
    }

    public static String jmmSymbolToOllirSymbol(Symbol symbol) {
        String name = symbol.getName();
        Type type = symbol.getType();

        StringBuilder ret = new StringBuilder(name).append(".");

        if (type.isArray()) {
            ret.append("array.");
        }

        ret.append(jmmTypeToOllirType(type.getName()));
        return ret.toString();
    }

    public static int getIdentantion() {
        return identantion;
    }

    public static void increaseIdentation() {
        identantion++;
    }

    public static void decreaseIdentation() {
        identantion--;
    }

    public static String getIdentationString() {
        return "\t".repeat(identantion);
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
                        evi.setOllirType(OllirGenerator.jmmTypeToOllirType(local.getType()));
                    }
                }
            }
            case PARAM -> {
                for (int i = 0; i < method.getArguments().size(); i++) {
                    Symbol param = method.getArguments().get(i);
                    if (param.getName().equals(jmmVarName)) {
                        evi.setResultName("$" + i + "." + jmmVarName);
                        evi.setOllirType(OllirGenerator.jmmTypeToOllirType(param.getType()));
                    }
                }

            }
            case FIELD -> {
                for (Symbol field : symbolTable.getFields()) {
                    if (field.getName().equals(jmmVarName)) {
                        String varAux = "aux" + tempVariables;
                        this.tempVariables++;
                        String fieldType = OllirGenerator.jmmTypeToOllirType(symbolInfo.getSymbol().getType());
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

            ret.append(jmmSymbolToOllirSymbol(argument));

            if (i != method.getArguments().size() - 1) { // is not last element
                ret.append(", ");
            }
        }

        ret.append(").").append(jmmTypeToOllirType(method.getRetType().getName())).append(" {\n");
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
        StringBuilder ret = new StringBuilder();

        if ((boolean) node.getObject("isArray")) {
            ret.append("array.");
        }

        ret.append(jmmTypeToOllirType(node.get("typeName")));
        return ret.toString();
    }

    private String dealWithScopeStatement(JmmNode node, String __) {
        StringBuilder ret = new StringBuilder("{\n");
        increaseIdentation();
        for (JmmNode child : node.getChildren()) {
            ret.append(visit(child)).append("\n");
        }
        ret.append("}\n\n");
        decreaseIdentation();
        return ret.toString();
    }

    private String dealWithConditionalStatement(JmmNode node, String methodName) { //TODO add GOTOs
        StringBuilder ret = new StringBuilder();

        // BAD CODE !! If and While should have different types, but as to not interfere with other branches this is a patchwork solution
        if (node.getNumChildren() == 3) { // if
            ret.append(getIdentationString()).append("if (");
            ret.append(visit(node.getJmmChild(0), methodName)).append(") ").append(visit(node.getJmmChild(1), methodName)).append("\nelse ").append(visit(node.getJmmChild(2))).append("\n");
        } else { // while
            ret.append(getIdentationString()).append("while (");
            ret.append(visit(node.getJmmChild(0), methodName)).append(") ").append(visit(node.getJmmChild(1), methodName)).append("\n");
        }

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

        return exprInfoToString(info) + ";\n\n";
    }

    private String dealWithClassFieldAssignmentStatement(JmmNode node, String methodName) {

        String fieldName = node.getJmmChild(0).get("varName");

        String fieldType = null;

        //TODO create method for this in symbolTable
        for (Symbol symbol : symbolTable.getFields()) {
            if (symbol.getName().equals(fieldName)) {
                fieldType = jmmTypeToOllirType(symbol.getType());
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
            return exprAuxInfoToString(assignedExprNodeData) + getIdentationString() + "putfield(this, " + jmmSymbolToOllirSymbol(symbolInfoOpt.get().getSymbol()) + ", " + assignedExprNodeData.getResultNameAndType() + ").V;\n\n";
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
        return exprAuxInfoToString(leftVarData) + exprAuxInfoToString(assignedExprNodeData) + getIdentationString() + varName + "." + assignedExprNodeData.getOllirType()
                + ":=." + assignedExprNodeData.getOllirType() + " " + assignedExprNodeData.getResultNameAndType() + ";\n\n";
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

        return exprAuxInfoToString(indexExpressionData) + exprAuxInfoToString(assignedExpressionData) + getIdentationString() +
                varName + "[" + indexExpressionData.getResultNameAndType() + "]." + getArrayOllirType(arrayVarInfo.getSymbol().getType())
                +  " :=." + assignedExpressionData.getOllirType() + " " + assignedExpressionData.getResultNameAndType() + ";\n\n";
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
