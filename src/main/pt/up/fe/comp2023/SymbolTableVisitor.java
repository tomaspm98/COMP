package pt.up.fe.comp2023;

import java.util.List;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2023.node.information.Expression;
import pt.up.fe.comp2023.node.information.Method;

public class SymbolTableVisitor extends PreorderJmmVisitor<String, String> {
    private final SymbolTableDeveloper table;

    @Override
    protected void buildVisitor() {
        addVisit("ImportDeclaration", this::dealWithImportDeclaration);
        addVisit("MethodDeclaration", this::dealWithMethodDeclaration);
        addVisit("ClassDeclaration", this::dealWithClassDeclaration);
        addVisit("Parameters", this::dealWithParamDeclaration);
        addVisit("Variables", this::dealWithVarDeclaration);
        addVisit("Type", this::dealWithType);
    }

    public SymbolTableVisitor(SymbolTableDeveloper table) {
        this.table = table;
        this.buildVisitor();
    }

    private void dealWithImportDeclaration(JmmNode node) {
        String ret = new String();
        for (JmmNode child : node.getChildren()) {
            ret += child.get("importPath") + (child.getIndexOfSelf() == node.getChildren().size() - 1 ? "" : ".");
        }
        this.table.addImport(ret);
    }

    private void dealWithClassDeclaration(JmmNode node) {
        table.setClassName(node.get("methodName"));
        if (node.hasAttribute("superClassName"))
            table.setSuper(node.get("superClassName"));
    }

    private void dealWithMethodDeclaration(JmmNode node) {
        String methodName = node.get("methodName");
        if (methodName.equals("main")) {
            table.addMethod("main", "void");
        } else {
            String typeName = node.getJmmChild(0).get("typeName"); //TODO check if this works...
            table.addMethod(methodName, typeName);
        }
    }

    private void dealWithParamDeclaration(JmmNode node, Method method) {
        // node.type = paramDeclaration

        String paramName = node.get("paramName");

        JmmNode type = node.getJmmChild(0);
        String typeName = visit(type);
        Type paramType = new Type(typeName, typeName.equals("IntArray"));

        method.addParameter(new Symbol(paramType, paramName));
    }

    private void dealWithStatement(JmmNode node, Method method) throws Exception {

        if (node.getKind().equals("If") || node.getKind().equals("WhileLoop")) {
            JmmNode condition = node.getJmmChild(0);
            Expression e = dealWithExpression(condition);
            if (!e.isCondition())
                throw new Exception("Invalid condition on " +
                        (node.getKind().equals("If") ? "'if' block" : "'while loop'") + //TODO export this to a function
                        ". Check the expression.");
            return;
        }
        // if is statement: TODO: check if the name of the newly assigned variable is already in scope (is a method parameter)


    }

    private Expression dealWithExpression(JmmNode node) {
        return new Expression();
    }

    private String dealWithType(JmmNode node) throws Exception {
        switch (node.getKind()) {
            case "IntArray":
                return "IntArray";
            case "Bool":
                return "Bool";
            case "Int":
                return "Int";
            case "CustomType":
                return node.get("typeName");
            default:
                throw new Exception("node.getKind() = '" + node.getKind() + "' was unexpected when dealing in dealWithType");
        }
    }

    private Symbol dealWithVarDeclaration(JmmNode node, String s) {
        String varName = node.get("name");

        JmmNode type = node.getJmmChild(0);
        String typeName = visit(type);
        Type varType = new Type(varName, typeName.equals("IntArray"));
        return new Symbol(varType, varName);
    }

}
