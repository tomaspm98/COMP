package pt.up.fe.comp2023.visitors;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;

public class OllirGenerator extends AJmmVisitor<String, String> {

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("ImportDeclaration", this::dealWithImportDeclaration);
        addVisit("MethodDeclaration", this::dealWithMethodDeclaration);
        addVisit("ClassDeclaration", this::dealWithClassDeclaration);
        addVisit("MethodName", this::dealWithNameNode);
        addVisit("SuperClassName", this::dealWithNameNode);
        addVisit("ClassName", this::dealWithNameNode);
        addVisit("Modifier", this::dealWithModifier);
        addVisit("Argument", this::dealWithArgument);
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        addVisit("MethodStatement", this::dealWithMethodStatement);
        addVisit("MethodReturnExpression", this::dealWithMethodReturnExpression);
        addVisit("IntExpression", this::dealWithIntExpression);
        addVisit("Statement", this::dealWithStatement);
        addVisit("Condition", this::dealWithCondition);
        addVisit("IfTrue", this::dealWithIfBranches);
        addVisit("ElseBlock", this::dealWithIfBranches);
        addVisit("WhileBlock", this::dealWithWhileBlock);
        addVisit("FieldDeclaration", this::dealWithFieldDeclaration);
        addVisit("Type", this::dealWithType);
    }

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

        String modifiers = node.getChildren().stream()
                .filter((child) -> child.getKind().equals("methodSymbols"))
                .map(modifierChild -> modifierChild.get("value"))
                .reduce("", (prevMod, nextMod) -> prevMod + " " + nextMod);
        ret.append(modifiers).append(" ");

        if (node.getKind().equals("NonVoid")) {
            //TODO do something
            String methodSymbol;
        }


        return ret.toString();
    }

    private String dealWithFieldDeclaration(JmmNode node, String __) {
        return parseSymbol(node) + ";";
    }

    private String dealWithVarDeclaration(JmmNode node, String __) {
        return "";
    }

    private String dealWithType(JmmNode node, String __) {
        StringBuilder ret = new StringBuilder();

        if ((boolean) node.getObject("isArray")) {
            ret.append("array.");
        }

        ret.append(node.get("typeName"));
        return ret.toString();
    }

    private String parseSymbol(JmmNode node) {
        StringBuilder ret = new StringBuilder(".field private ");

        String fieldType = visit(node.getJmmChild(0));
        String fieldName = node.get("name");

        ret.append(fieldName).append(".").append(fieldType);
        return ret.toString();
    }
}
