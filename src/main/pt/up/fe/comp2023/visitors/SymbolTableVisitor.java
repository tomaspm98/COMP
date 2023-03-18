package pt.up.fe.comp2023.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.SymbolTable;
import pt.up.fe.comp2023.node.information.Expression;
import pt.up.fe.comp2023.node.information.Method;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class SymbolTableVisitor extends AJmmVisitor<String, String> {
    private final SymbolTable table;

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
    }

    public SymbolTableVisitor(SymbolTable table) {
        this.table = table;
        this.buildVisitor();
    }

    public SymbolTableVisitor() {
        this.table = new SymbolTable();
        this.buildVisitor();
    }

    // UTILITY FUNCTIONS
    private Method getMethodByName(String name) {
        return table.getFullMethods().stream().filter(method -> method.getName().equals(name)).toList().get(0);
    }

    private boolean isInScope(String varName, Type varType, Method method) {

        List<Symbol> symbolsInScopeWithSameName = new ArrayList<>();

        Symbol parameterWithSameName = method.getArguments().stream()
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
            if (symbol.getType().equals(varType)) {
                return true;
            }
        return false;
    }

    // END UTILITY FUNCTIONS

    private String dealWithProgram(JmmNode node, String s) {
        for (JmmNode child : node.getChildren()) {
            visit(child);
        }
        return "";
    }

    private String dealWithImportDeclaration(JmmNode node, String s) {
        StringBuilder ret = new StringBuilder();
        for (JmmNode child : node.getChildren()) {
            ret.append(child.get("pathFragment")).append(child.getIndexOfSelf() == node.getChildren().size() - 1 ? "" : ".");
        }
        this.table.addImport(ret.toString());
        return "";
    }

    private String dealWithClassDeclaration(JmmNode node, String s) {
        String className = node.get("className");
        table.setClassName(className);
        if (node.hasAttribute("superClassName")) {
            String superClassName = node.get("superClassName");
            table.setSuper(superClassName);
        }
        for (JmmNode child : node.getChildren()) {
            visit(child, "");
        }
        return "";
    }

    private String dealWithFieldDeclaration(JmmNode node, String s) {
        String fieldName = node.get("name");
        Type fieldType = dealWithType(node.getJmmChild(0));
        this.table.addField(new Symbol(fieldType, fieldName));
        return "";
    }

    private String dealWithMethodDeclaration(JmmNode node, String s) {
        Symbol methodSymbol = dealWithMethodSymbol(node.getChildren().stream().filter(aChild -> aChild.getKind().equals("MethodSymbol")).toList().get(0));
        Method method = new Method(methodSymbol.getName(), methodSymbol.getType());
        table.addMethod(method);
        for (JmmNode child : node.getChildren()) {
            if (child.getKind().equals("MethodSymbol")) {
                continue;
            } else {
                visit(child, methodSymbol.getName());
            }
        }
        return "";
    }

    private Symbol dealWithMethodSymbol(JmmNode node) {
        String name = node.get("name");
        Type type = dealWithType(node.getJmmChild(0));
        return new Symbol(type, name);
    }

    private String dealWithModifier(JmmNode node, String methodName) {
        String modifier = node.get("value");
        Method parentMethod = getMethodByName(methodName);
        parentMethod.addModifier(modifier); // TODO: if modifier == private check if there's not a public already; if modifier == public check if there's not a private already
        return "";
    }

    private String dealWithArgument(JmmNode node, String methodName) {
        String argumentName = node.get("name");
        Method method = this.getMethodByName(methodName);

        JmmNode type = node.getJmmChild(0);
        Type argumentType = dealWithType(type);
        method.addArgument(new Symbol(argumentType, argumentName));
        return "";
    }

    private String dealWithVarDeclaration(JmmNode node, String methodName) {
        Method method = this.getMethodByName(methodName);
        JmmNode typeNode = node.getJmmChild(0);
        Type varType = dealWithType(typeNode);
        String varName = node.get("name");
        method.addVariable(new Symbol(varType, varName));
        return "";
    }

    private String dealWithMethodStatement(JmmNode node, String methodName) {
        visit(node.getJmmChild(0)); // TODO something else?
        return "";
    }

    private String dealWithMethodReturnExpression(JmmNode node, String methodName) {
        Method method = getMethodByName(methodName);
        Type methodReturnType = method.getRetType();

        Expression retExpr = dealWithExpression(node.getJmmChild(0));

        //TODO for semanticAnalysis
        /*if (!retExpr.getRetType().equals(methodReturnType)) {
            System.err.println("Return type does not match method's return type.");
            exit(1);
        }*
         */
        return "";
    }

    private String dealWithStatement(JmmNode node, String methodName) {

        // TODO for semanticAnalysis
        //Method method = getMethodByName(methodName);

        if (node.getKind().equals("Conditional")) {
            for (JmmNode child : node.getChildren()) {
                visit(child);
            }
        } else if (node.getKind().equals("ClassFieldAssignment")) {
            JmmNode assignedExpressionNode = node.getJmmChild(1);
            Expression assignedExpression = dealWithExpression(assignedExpressionNode);
            // TODO for semanticAnalysis
        } else if (node.getKind().equals("Assignment") || node.getKind().equals("ArrayAssignment")) {
            JmmNode assignedExpressionNode;
            if (node.hasAttribute("arrayIndex")) {
                dealWithIntExpression(node.getJmmChild(0), "");
                assignedExpressionNode = node.getJmmChild(1);
            } else
                assignedExpressionNode = node.getJmmChild(0);
            String variableName = node.get("varName");
            Expression assignedExpression = dealWithExpression(assignedExpressionNode);
            // TODO for semanticAnalysis
            /*if (!isInScope(variableName, assignedExpression.getRetType(), method)) {
                System.err.println("Trying to access a variable that is not in scope");
                exit(1);
            }*/
        } else { // Scope or SimpleStatement
            for (JmmNode child : node.getChildren()) {
                visit(child, "");
            }
        }
        return "";
    }

    private String dealWithIfBranches(JmmNode node, String s) {
        visit(node.getJmmChild(0));
        return "";
    }

    private String dealWithWhileBlock(JmmNode node, String s) {
        visit(node.getJmmChild(0));
        return "";
    }

    private String dealWithCondition(JmmNode node, String s) {
        Expression expression = dealWithExpression(node.getJmmChild(0));

        //TODO for semanticAnalysis
        /*
        if (!expression.getRetType().equals(new Type("boolean", false))) {
            System.err.println("Condition expression isn't a boolean");
            exit(1);
        }
        */

        return "";
    }

    private String dealWithIntExpression(JmmNode node, String s) {
        Expression expression = dealWithExpression(node.getJmmChild(0));

        //TODO for semanticAnalysis
        /*
        if (!expression.getRetType().equals(new Type("int", false))) {
            System.err.println("Trying to access an array with an expression of type != int");
            exit(1);
        }
         */
        return "";
    }

    private Expression dealWithMethodCall(JmmNode node) {
        return new Expression();
    }

    private Expression dealWithArrayLength(JmmNode node) {
        return new Expression();
    }

    private Expression dealWithParenthesis(JmmNode node) {
        return new Expression();
    }

    private Expression dealWithUnaryBinaryOp(JmmNode node) {
        return new Expression();
    }

    private Expression dealWithArithmeticBinaryOp(JmmNode node) {
        return new Expression();
    }

    private Expression dealWithBoolBinaryOp(JmmNode node) {
        return new Expression();
    }

    private Expression dealWithInstantiation(JmmNode node) {
        return new Expression();
    }

    private Expression dealWithInteger(JmmNode node) {
        return new Expression();
    }

    private Expression dealWithBoolean(JmmNode node) {
        return new Expression();
    }

    private Expression dealWithIdentifier(JmmNode node) {
        return new Expression();
    }

    private Expression dealWithClassAccess(JmmNode node) {
        return new Expression();
    }

    private Expression dealWithClassField(JmmNode node) {
        return new Expression();
    }

    private Expression dealWithExpression(JmmNode node) {
        switch (node.getKind()) {
            case "MethodCall" -> {
                return dealWithMethodCall(node);
            }
            case "ArrayLength" -> {
                return dealWithArrayLength(node);
            }
            case "Parenthesis" -> {
                return dealWithParenthesis(node);
            }
            case "UnaryBinaryOp" -> {
                return dealWithUnaryBinaryOp(node);
            }
            case "ArithmeticBinaryOp" -> {
                return dealWithArithmeticBinaryOp(node);
            }
            case "BoolBinaryOp" -> {
                return dealWithBoolBinaryOp(node);
            }
            case "ArrayInstantiation", "Instantiation" -> {
                return dealWithInstantiation(node);
            }
            case "Integer" -> {
                return dealWithInteger(node);
            }
            case "Boolean" -> {
                return dealWithBoolean(node);
            }
            case "Identifier" -> {
                return dealWithIdentifier(node);
            }
            case "ClassAccess" -> {
                return dealWithClassAccess(node);
            }
            case "ExplicitClassFieldAccess" -> {
                return dealWithClassField(node);
            }
            default -> {
                System.err.println("Found an expression node with unknown type: " + node.getKind());
            }
        }

        // tratar de saber se esta num statement e lidar com isso ou avaliar o tipo de expressao

        // just for java to stop throwing warnings... will never be reached
        return new Expression();
    }

    private Type dealWithType(JmmNode node) {
        String typeName = node.get("typeName");
        boolean isArray = (boolean) node.getObject("isArray");
        // TODO for semanticAnalysis
        /*
        if (typeName.equals("void") && isArray) {
            System.err.println("Invalid return type: Void[]");
            exit(1);
        }
         */
        return new Type(typeName, isArray);
    }

    private String dealWithNameNode(JmmNode nameNode, String s) {
        return nameNode.get("name");
    }

}
