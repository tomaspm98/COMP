package pt.up.fe.comp2023.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.SymbolTable;
import pt.up.fe.comp2023.node.information.Expression;
import pt.up.fe.comp2023.node.information.Method;
import pt.up.fe.specs.util.collections.SpecsList;

import java.util.ArrayList;
import java.util.List;



public class SymbolTableVisitor extends AJmmVisitor<String, String> {
    private final SymbolTable table;

    private List<Report> reports;

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
        addVisit("ArrayAccess", this::visitArrayAccess);
        setDefaultVisit(this::defaultVisitor);
    }

    public SymbolTableVisitor(SymbolTable table) {
        this.table = table;
        this.reports = SpecsList.newInstance(Report.class);
        this.buildVisitor();
    }

    public SymbolTableVisitor() {
        this.table = new SymbolTable();
        this.reports = SpecsList.newInstance(Report.class);
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

        symbolsInScopeWithSameName.add(classFieldWithSameName);


        for (Symbol symbol : symbolsInScopeWithSameName)
            if (symbol.getType().equals(varType)) {
                return true;
            }
        return false;
    }

    // END UTILITY FUNCTIONS

    public String visitArrayAccess(JmmNode node, String dummy) {
        // Infer the type of the array and index, e.g., using a getType method
        String arrayType = getType(node.getJmmChild(0));
        String indexType = getType(node.getJmmChild(1));

        // Check if the types are valid, e.g., the arrayType is an array and indexType is an integer
        if (arrayType.endsWith("[]") && indexType.equals("int")) {
            // Set the inferred type of the ArrayAccess node to the element type of the array
            String elementType = arrayType.substring(0, arrayType.length() - 2);
            node.put("type", elementType);
        } else {
            int line = Integer.parseInt(node.get("line"));
            int col = Integer.parseInt(node.get("col"));
            String errorMsg = "Invalid array access: expected an array type and an integer index, but found " + arrayType + " and " + indexType;
            Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, line, col, errorMsg);
            reports.add(report);
        }

        return "";
    }

    public String getType(JmmNode node) {
        String type = "";

        if (node.getKind().equals("Identifier")) {
            String varName = node.get("value");
            var tempMethod = node.getAncestor("MethodDeclaration");

            if (tempMethod != null) {
                String methodName = tempMethod.get().getJmmChild(0).get("value");

                for (var localVariable : table.getLocalVariables(methodName)) {
                    if (localVariable.getName().equals(varName)) {
                        return localVariable.getType().getName();
                    }
                }

                for (var localParameter : table.getParameters(methodName)) {
                    if (localParameter.getName().equals(varName)) {
                        return localParameter.getType().getName();
                    }
                }
            }

            var fields = table.getFields();

            for (var field : fields) {
                if (varName.equals(field.getName())) {
                    return field.getType().getName();
                }
            }
        } else if (node.getKind().equals("Literal")) {
            String literalType = node.get("type");
            if (literalType != null) {
                return literalType;
            }
        } else if (node.getKind().equals("NewArray")) {
            return "int[]"; // Assuming the only possible array type is int[]
        } else if (node.getKind().equals("ArrayAccess")) {
            String arrayType = getType(node.getJmmChild(0));
            if (arrayType.endsWith("[]")) {
                type = arrayType.substring(0, arrayType.length() - 2);
            }
        } else if (node.getKind().equals("MethodCall")) {
            // You'll need to handle method call type inference based on the called method's return type
        } else if (node.getKind().equals("BinaryExpression") || node.getKind().equals("UnaryExpression")) {
            // You'll need to handle expression type inference based on the operands and the operator
        }

        return type;
    }

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
            if (child.getIndexOfSelf() == node.getChildren().size() - 1) {
                this.table.addImportedClass(child.get("pathFragment"));
            }
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

    private boolean isMethodSymbol(JmmNode node) {
        return node.getKind().equals("MethodSymbol") || node.getKind().equals("VoidMethodSymbol");
    }

    private String defaultVisitor(JmmNode __, String ___) {
        return "";
    }

    private String dealWithMethodDeclaration(JmmNode node, String s) {
        Symbol methodSymbol = dealWithMethodSymbol(node.getChildren().stream().filter(this::isMethodSymbol).toList().get(0));
        Method method = new Method(methodSymbol.getName(), methodSymbol.getType());

        table.addMethod(method);
        for (JmmNode child : node.getChildren()) {
            if (isMethodSymbol(child)) {
                continue;
            } else {
                visit(child, methodSymbol.getName());
            }
        }
        return "";
    }

    private Symbol dealWithMethodSymbol(JmmNode node) {
        String name = node.get("name");
        Type type;
        if (node.getKind().equals("MethodSymbol"))
            type = dealWithType(node.getJmmChild(0));
        else
            type = new Type("void", false);

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

        return "";
    }

    private String dealWithStatement(JmmNode node, String methodName) {

        if (node.getKind().equals("Conditional")) {
            for (JmmNode child : node.getChildren()) {
                visit(child);
            }
        } else if (node.getKind().equals("ClassFieldAssignment")) {
            JmmNode assignedExpressionNode = node.getJmmChild(1);
            Expression assignedExpression = dealWithExpression(assignedExpressionNode);
        } else if (node.getKind().equals("Assignment") || node.getKind().equals("ArrayAssignment")) {
            JmmNode assignedExpressionNode;
            if (node.hasAttribute("arrayIndex")) {
                dealWithIntExpression(node.getJmmChild(0), "");
                assignedExpressionNode = node.getJmmChild(1);
            } else
                assignedExpressionNode = node.getJmmChild(0);
            Expression assignedExpression = dealWithExpression(assignedExpressionNode);
        } /*else { // Scope or SimpleStatement
            for (JmmNode child : node.getChildren()) {
                visit(child, "");
            }
        }*/
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
        return "";
    }

    private String dealWithIntExpression(JmmNode node, String s) {
        Expression expression = dealWithExpression(node.getJmmChild(0));
        return "";
    }

    private Expression dealWithExpression(JmmNode node) {
        switch (node.getKind()) {
            case  "MethodCall", "ArrayLength", "Parenthesis", "UnaryBinaryOp", "ArithmeticBinaryOp",
                    "BoolBinaryOp", "ArrayInstantiation", "Instantiation", "Integer", "Boolean",
                    "Identifier", "ClassAccess", "ExplicitClassFieldAccess", "ArrayAccess" -> {
                return new Expression();
            }
            default -> {
                System.err.println("Found an expression node with unknown type: " + node.getKind());
                Report report = new Report(ReportType.ERROR, Stage.SYNTATIC, Integer.parseInt(node.get("lineStart")), "Expression node with unknown type: " + node.getKind());
                this.reports.add(report);
            }
        }
        // just for java to stop throwing warnings... will never be reached
        return new Expression();
    }

    private Type dealWithType(JmmNode node) {
        String typeName = node.get("typeName");
        boolean isArray = (boolean) node.getObject("isArray");

        return new Type(typeName, isArray);
    }

    private String dealWithNameNode(JmmNode nameNode, String s) {
        return nameNode.get("name");
    }

    public List<Report> getReports() {
        return reports;
    }

    public void addReport(Report report) {
        reports.add(report);
    }
}
