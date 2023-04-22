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
import pt.up.fe.specs.util.collections.SpecsList;

import java.util.List;
import java.util.Optional;

public class OllirGenerator extends AJmmVisitor<String, String> {

    private final SymbolTable symbolTable;
    private final SpecsList<Report> reports;
    private int tempVariables;

    public OllirGenerator(SymbolTable symbolTable, SpecsList<Report> reports) {
        super();
        this.symbolTable = symbolTable;
        this.reports = reports;
        this.tempVariables = 0;
    }

    public static String jmmTypeToOllirType(String jmmType) {
        switch (jmmType) {
            case "int" -> {
                return "i32";
            }
            case "boolean" -> {
                return "bool";
            }
            default -> {
                return jmmType;
            }
        }
    }

    // Utility functions

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

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("ImportDeclaration", this::dealWithImportDeclaration);
        addVisit("MethodDeclaration", this::dealWithMethodDeclaration);
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        addVisit("FieldDeclaration", this::dealWithFieldDeclaration);
        addVisit("Type", this::dealWithType);
    }

    private String jmmSymbolToOllirSymbol(JmmNode node) {
        StringBuilder ret = new StringBuilder();

        String fieldType = visit(node.getJmmChild(0));
        String fieldName = node.get("name");

        ret.append(fieldName).append(".").append(fieldType);
        return ret.toString();
    }

    private String getMethodName(JmmNode methodNode) {
        String ret;

        if (methodNode.getKind().equals("Void"))
            ret = methodNode.getObject("VoidMethodSymbol", JmmNode.class).get("name");
        else ret = methodNode.getObject("MethodSymbol", JmmNode.class).get("name");

        return ret;
    }

    private String exprInfoToString(ExpressionVisitorInformation data) {
        StringBuilder ret = new StringBuilder();

        for (var auxLine : data.getAuxLines()) {
            ret.append(auxLine).append("\n");
        }

        ret.append(data.getResultNameAndType());
        return ret.toString();
    }

    private String exprAuxInfoToString(ExpressionVisitorInformation data) {
        StringBuilder ret = new StringBuilder();

        for (var auxLine : data.getAuxLines()) {
            ret.append(auxLine).append("\n");
        }

        return ret.toString();
    }

    public static String getOllirVariableNameAndType(SymbolTable symbolTable, Method method, String jmmVarName) {
        SymbolInfo symbolInfo = symbolTable.getMostSpecificSymbol(method.getName(), jmmVarName);



        switch(symbolInfo.getSymbolPosition()) {
            case LOCAL -> {
                for (Symbol local : method.getVariables()) {
                    if (local.getName().equals(jmmVarName)) {
                        return jmmVarName + "." + OllirGenerator.jmmTypeToOllirType(local.getType());
                    }
                }
            }
            case PARAM -> {
                for (int i = 0; i < method.getArguments().size(); i++) {
                    Symbol param = method.getArguments().get(i);
                    if (param.getName().equals(jmmVarName)) {
                        return "$" + i + "." + jmmVarName + "." + OllirGenerator.jmmTypeToOllirType(local.getType());
                    }
                }

            }
            case FIELD -> {
                for (Symbol field : symbolTable.getFields()) {
                    if (field.getName().equals(value)) {
                        //TODO
                        return jmmVarName + "." + OllirGenerator.jmmTypeToOllirType(local.getType());
                        //TODO
                        String varAux = getNewAuxVariable();
                        String fieldType = OllirGenerator.jmmTypeToOllirType(symbolInfo.getSymbol().getType());
                        String auxLine = varAux + "." + fieldType + " :=." + fieldType + " getfield(this, " + field.getName() + "." + fieldType + ")." + fieldType;
                        ret.addAuxLine(auxLine);
                        ret.setResultName(varAux);
                        ret.setOllirType(fieldType);
                    }
                }
            }
        }

    }


    // Visitors

    private String dealWithProgram(JmmNode node, String arg) {
        StringBuilder ret = new StringBuilder();
        for (JmmNode child : node.getChildren()) {
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
        ret.append(lastFragment);
        return ret.toString();
    }

    private String dealWithMethodDeclaration(JmmNode node, String __) {
        StringBuilder ret = new StringBuilder(".method ");

        String methodName = this.getMethodName(node);
        Optional<Method> methodOp = this.symbolTable.getMethodTry(methodName);

        if (methodOp.isEmpty()) {
            System.err.println("Tried to get method with name '" + methodName + "' but it wasn't found in the symbol table");
            System.exit(1);
        }

        Method method = methodOp.get();

        for (int i = 0; i < method.getModifiers().size(); i++) {
            ret.append(method.getModifiers().get(i));

            if (i != method.getModifiers().size() - 1) { // is not last element
                ret.append(" ");
            }
        }

        ret.append(methodName).append("(");

        for (int i = 0; i < method.getArguments().size(); i++) {
            Symbol argument = method.getArguments().get(i);

            ret.append(jmmSymbolToOllirSymbol(argument));

            if (i != method.getModifiers().size() - 1) { // is not last element
                ret.append(", ");
            }
        }

        ret.append(").").append(jmmTypeToOllirType(method.getRetType().getName())).append(" {\n");

        List<JmmNode> methodStatements = node.getChildren().stream().filter((child) -> child.getKind().equals("MethodStatement")).map((child) -> child.getJmmChild(0)) // get statement inside methodStatement
                .toList();

        if (node.getKind().equals("NonVoid")) {
            ret.append("ret.");
            JmmNode retExpressionNode = node.getChildren().get(node.getNumChildren() - 1);
            //get return expression type and append;

            //append expression and ret type;
        }
        ret.append("}\n\n");


        return ret.toString();
    }

    private String dealWithFieldDeclaration(JmmNode node, String __) {
        return ".field private " + jmmSymbolToOllirSymbol(node) + ";";
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
        for (JmmNode child : node.getChildren()) {
            ret.append("    ").append(visit(child)).append("\n");
        }
        ret.append("}\n");
        return ret.toString();
    }

    private String dealWithConditionalStatement(JmmNode node, String __) { //TODO add GOTOs
        StringBuilder ret = new StringBuilder();

        // BAD CODE !! If and While should have different types, but as to not interfere with other branches this is a patchwork solution
        if (node.getNumChildren() == 3) { // if
            ret.append("if (");
            ret.append(visit(node.getJmmChild(0))).append(") ").append(visit(node.getJmmChild(1))).append("\nelse ").append(visit(node.getJmmChild(2))).append("\n");
        } else { // while
            ret.append("while (");
            ret.append(visit(node.getJmmChild(0))).append(") ").append(visit(node.getJmmChild(1))).append("\n");
        }

        return ret.toString();
    }

    private String dealWithCondition(JmmNode node, String __) {
        ExpressionVisitor exprVisitor = new ExpressionVisitor(symbolTable, this.tempVariables);
        ExpressionVisitorInformation info = exprVisitor.visit(node.getJmmChild(0));
        this.tempVariables += exprVisitor.getUsedAuxVariables();
        return exprInfoToString(info);
    }

    private String dealWithIfTrue(JmmNode node, String __) {
        return visit(node.getJmmChild(0));
    }

    private String dealWithElseBlock(JmmNode node, String __) {
        return visit(node.getJmmChild(0));
    }

    private String dealWithSimpleStatement(JmmNode node, String __) {
        ExpressionVisitor exprVisitor = new ExpressionVisitor(symbolTable, this.tempVariables);
        ExpressionVisitorInformation info = exprVisitor.visit(node.getJmmChild(0));
        this.tempVariables += exprVisitor.getUsedAuxVariables();

        return exprInfoToString(info) + ";\n";
    }

    private String dealWithClassFieldAssignmentStatement(JmmNode node, String __) {

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
        ExpressionVisitorInformation info = exprVisitor.visit(node.getJmmChild(0));
        this.tempVariables += exprVisitor.getUsedAuxVariables();
        return exprInfoToString(info) + "putfield(this, " + fieldName + "." + fieldType + ", " + info.getResultNameAndType() + ").V;\n";
    }

    // Please do not read this code. This is the worst code I have ever written.
    private String dealWithAssignmentStatement(JmmNode node, String methodName) {

        String varName = node.get("varName");

        JmmNode exprNode =  node.getJmmChild(0);

        Optional<Method> optMethod = this.symbolTable.getMethodTry(methodName);

        if (optMethod.isEmpty()) {
            //TODO maybe add report
            System.err.println("Tried to search for method '" + methodName + "' but it wasn't found.");
            return null;
        }

        String ollirVarName = getOllirVariableName(Method method, String jmmVarName);

        ExpressionVisitor expressionVisitor = new ExpressionVisitor(this.symbolTable, this.tempVariables);
        ExpressionVisitorInformation info = expressionVisitor.visit(exprNode);
        String ret = exprAuxInfoToString(info) + varName + "." + ;


        return "";
    }

    private String dealWithArrayAssignmentStatement(JmmNode node, String __) {

        return "";
    }
}
