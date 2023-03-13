package pt.up.fe.comp2023.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2023.SymbolTableDeveloper;
import pt.up.fe.comp2023.node.information.Expression;
import pt.up.fe.comp2023.node.information.Method;
import pt.up.fe.specs.util.collections.SpecsCollection;
import pt.up.fe.specs.util.collections.SpecsList;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.err;
import static java.lang.System.exit;

public class SymbolTableVisitor extends PreorderJmmVisitor<String, String> {
    private final SymbolTableDeveloper table;

    @Override
    protected void buildVisitor() {
        addVisit("importDeclaration", this::dealWithImportDeclaration);
        addVisit("methodDeclaration", this::dealWithMethodDeclaration);
        addVisit("classDeclaration", this::dealWithClassDeclaration);
    }



    public SymbolTableVisitor(SymbolTableDeveloper table) throws Exception {
        this.table = table;
        this.buildVisitor();
    }

    private String dealWithImportDeclaration(JmmNode node, String s) {
        StringBuilder ret = new StringBuilder();
        for (JmmNode child : node.getChildren()) {
            ret.append(child.get("importPath")).append(child.getIndexOfSelf() == node.getChildren().size() - 1 ? "" : ".");
        }
        this.table.addImport(ret.toString());
        return "";
    }

    private String dealWithClassDeclaration(JmmNode node, String s) {
        table.setClassName(node.get("methodName"));
        if (node.hasAttribute("superClassName"))
            table.setSuper(node.get("superClassName"));
        return "";
    }

    private String dealWithMethodDeclaration(JmmNode node, String s) {
        String methodName = node.get("methodName");
        if (methodName.equals("main")) {
            Method method = new Method("main", "void");

            try {
                JmmNode paramType = (JmmNode) node.getOptionalObject("paramType")
                        .orElseThrow(() -> new Exception("Parameter in main method declaration has no 'paramType' attribute."));
                method.addParameter(new Symbol(dealWithType(paramType), "paramName"));
            } catch (Exception e) {
                System.out.println("Found exception while trying to handle type in dealWithMethodDeclaration: " + e);
                exit(1);
            }

            table.addMethod(method);
        } else {
            String typeName = node.getJmmChild(0).get("typeName"); //TODO check if this works...
            Method method = new Method(methodName, typeName);
            table.addMethod(method);

        }
        return "";
    }

    private void dealWithParamDeclaration(JmmNode node, Method method) throws Exception {
        // node.type = paramDeclaration

        String paramName = node.get("paramName");

        JmmNode type = node.getJmmChild(0);
        Type paramType = dealWithType(type);

        method.addParameter(new Symbol(paramType, paramName));
    }

    private void throwInvalidCondition(JmmNode node) throws Exception {

        String kindInformer = switch (node.getKind()) {
            case "If" -> "'if' block";
            case "WhileLoop" -> "'while loop'";
            default -> throw new Exception("Unhandled kind = '" + node.getKind() + "' in throwInvalidCondition");
        };

        throw new Exception("Invalid condition on " + kindInformer + ".");
    }


    private boolean isInScope(JmmNode variable, Method method) throws Exception {
        String varName = variable.get("varName");
        Expression assignedExpr = (Expression) variable.getOptionalObject("assigned")
                .orElseThrow(() -> new Exception("isInScope was passed a node without 'assigned' attribute."));

        List<Symbol> symbolsInScopeWithSameName = new ArrayList<>();

        Symbol parameterWithSameName = method.getParameters().stream()
                .filter(var -> var.getName().equals(varName))
                .findFirst().orElse(null);
        Symbol methodVarWithSameName = method.getVariables().stream()
                .filter(var -> var.getName().equals(varName))
                .findFirst().orElse(null);
        Symbol classFieldWithSameName = table.getFields().stream()
                .filter(var -> var.getName().equals(varName))
                .findFirst().orElse(null);

        if (parameterWithSameName != null) symbolsInScopeWithSameName.add(parameterWithSameName);
        if (methodVarWithSameName != null && parameterWithSameName == null)
            symbolsInScopeWithSameName.add(methodVarWithSameName);
        if (classFieldWithSameName != null) symbolsInScopeWithSameName.add(classFieldWithSameName);

        symbolsInScopeWithSameName.add(classFieldWithSameName); /* TODO check with professor; are we allowed to
                                                                        access class fields without 'this.'? */

        for (Symbol symbol : symbolsInScopeWithSameName)
            if (symbol.getType().equals(assignedExpr.retType())) {
                return true;
            }
        return false;
    }


    private void dealWithStatement(JmmNode node, Method method) throws Exception {

        if (node.getKind().equals("If") || node.getKind().equals("WhileLoop")) {
            JmmNode condition = (JmmNode) node.getOptionalObject("condition").orElseThrow(() -> new Exception("Hi"));
            Expression e = dealWithExpression(condition);
            if (!e.isCondition())
                throwInvalidCondition(node);

            return;
        }
        // if is statement: TODO: check if the name of the newly assigned variable is already in scope (is a method parameter)
    }

    private Expression dealWithExpression(JmmNode node) {
        return new Expression();
    }

    private Type dealWithType(JmmNode node) throws Exception {
        String typeName =
                switch (node.getKind()) {
                    case "IntArray" -> "IntArray";
                    case "Bool" -> "Bool";
                    case "Int" -> "Int";
                    case "CustomType" -> node.get("typeName");
                    default ->
                            throw new Exception("node.getKind() = '" + node.getKind() + "' was unexpected when dealing in dealWithType");
                };
        return new Type(typeName, typeName.equals("IntArray"));
    }

    private Symbol dealWithVarDeclaration(JmmNode node, String s) {
        String varName = node.get("name");

        JmmNode type = node.getJmmChild(0);
        String typeName = visit(type);
        Type varType = new Type(varName, typeName.equals("IntArray"));
        return new Symbol(varType, varName);
    }

}
