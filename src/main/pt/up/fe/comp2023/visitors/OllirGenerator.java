package pt.up.fe.comp2023.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.SymbolTable;
import pt.up.fe.comp2023.node.information.Method;
import pt.up.fe.specs.util.collections.SpecsList;

import java.util.Optional;

public class OllirGenerator extends AJmmVisitor<String, String> {

    private final SymbolTable symbolTable;
    private final int tempVariables;
    private final SpecsList<Report> reports;

    public OllirGenerator(SymbolTable symbolTable, SpecsList<Report> reports) {
        super();
        this.symbolTable = symbolTable;
        this.reports = reports;
        this.tempVariables = 0;
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

    // Utility functions

    private String jmmTypeToOllirType(String jmmType) {
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

    private String jmmSymbolToOllirSymbol(JmmNode node) {
        StringBuilder ret = new StringBuilder();

        String fieldType = visit(node.getJmmChild(0));
        String fieldName = node.get("name");

        ret.append(fieldName).append(".").append(fieldType);
        return ret.toString();
    }

    private String jmmSymbolToOllirSymbol(Symbol symbol) {
        String name = symbol.getName();
        Type type = symbol.getType();

        StringBuilder ret = new StringBuilder(name).append(".");

        if (type.isArray()) {
            ret.append("array.");
        }

        ret.append(jmmTypeToOllirType(type.getName()));
        return ret.toString();
    }

    private String getMethodName(JmmNode methodNode) {
        String ret;

        if (methodNode.getKind().equals("Void"))
            ret = methodNode.getObject("VoidMethodSymbol", JmmNode.class).get("name");
        else
            ret = methodNode.getObject("MethodSymbol", JmmNode.class).get("name");

        return ret;
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

            ret.append(this.jmmSymbolToOllirSymbol(argument));

            if (i != method.getModifiers().size() - 1) { // is not last element
                ret.append(", ");
            }
        }

        ret.append(").")
                .append(this.jmmTypeToOllirType(method.getRetType().getName()))
                .append(" {\n");

        //TODO finish




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
}
