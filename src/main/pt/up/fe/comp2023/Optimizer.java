package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.node.information.Method;
import pt.up.fe.comp2023.visitors.OllirGenerator;
import pt.up.fe.specs.util.collections.SpecsList;

import java.util.ArrayList;
import java.util.List;

public class Optimizer implements JmmOptimization {

    private static JmmNode getMethodSymbolNode(JmmNode child) {
        if (child.getKind().equals("NonVoid")) {
            for (JmmNode child2 : child.getChildren()) {
                if (child2.getKind().equals("MethodSymbol")) {
                    return child2;
                }
            }
        } else {
            for (JmmNode child2 : child.getChildren()) {
                if (child2.getKind().equals("VoidMethodSymbol")) {
                    return child2;
                }
            }
        }
        return null;
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        SpecsList<Report> reports = (SpecsList<Report>) jmmSemanticsResult.getReports();
        JmmNode root = jmmSemanticsResult.getRootNode();
        OllirGenerator generator = new OllirGenerator((SymbolTable) jmmSemanticsResult.getSymbolTable(), reports);
        return new OllirResult(jmmSemanticsResult, generator.visit(root), reports);
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult jmmSemanticsResult) {
        SymbolTable st = (SymbolTable) jmmSemanticsResult.getSymbolTable();
        for (Method method : st.getFullMethods()) {
            JmmNode methodNode = getMethodNode(jmmSemanticsResult.getRootNode(), method.getName());
            for (Symbol declaredVariable : method.getVariables()) {
                if (declaredVariable.getType().isArray()) {
                    continue;
                }
                boolean isConstant = getNumberOfWrites(methodNode, declaredVariable) == 1;
                if (isConstant) {
                    turnIntoConstant(methodNode, declaredVariable);
                }
            }
        }

        return jmmSemanticsResult;
    }

    private void turnIntoConstant(JmmNode methodNode, Symbol declaredVariable) {
        List<JmmNode> methodStatements = getMethodStatements(methodNode);
        JmmNode assignedExpression = null;
LOOP1:  for (JmmNode methodStatement : methodStatements) {
            for (JmmNode assignment : getAssignmentStatements(methodStatement)) {
                if (assignment.get("varName").equals(declaredVariable.getName())) {
                    assignedExpression = assignment.getJmmChild(0);
                    JmmNode parent = assignment.getJmmParent();
                    parent.getJmmParent().removeJmmChild(parent);
                    assignment.getJmmParent().removeJmmChild(assignment);
                    break LOOP1;
                }
            }
        }

        if (assignedExpression == null) {
            System.err.println("No assignment found for variable " + declaredVariable.getName());
            return;
        }

        for (JmmNode node : getMethodIdentifierAccess(methodNode, declaredVariable.getName())) {
            JmmNode newNode = new JmmNodeImpl(assignedExpression.getKind(), assignedExpression);
            node.replace(newNode);
        }
    }

    private JmmNode getClassDeclarationNode(JmmNode root) {
        for (JmmNode child : root.getChildren()) {
            if (child.getKind().equals("ClassDeclaration")) {
                return child;
            }
        }
        return null;
    }

    private JmmNode getMethodNode(JmmNode root, String methodName) {

        JmmNode classDeclarationNode = getClassDeclarationNode(root);

        if (classDeclarationNode == null) {
            System.err.println("ClassDeclaration node not found");
            return null;
        }

        for (JmmNode child : classDeclarationNode.getChildren()) {
            if (child.getHierarchy().contains("MethodDeclaration") && getMethodSymbolNode(child).get("name").equals(methodName)) {
                return child;
            }
        }
        return null;
    }

    private int getNumberOfWrites(JmmNode methodNode, Symbol symbol) {
        int numberOfWrites = 0;
        for (JmmNode statement : getMethodStatements(methodNode)) {
            for (JmmNode assignment : getAssignmentStatements(statement)) {
                if (assignment.get("varName").equals(symbol.getName())) {
                    numberOfWrites++;
                }

            }
        }
        return numberOfWrites;
    }

    private List<JmmNode> getMethodStatements(JmmNode methodNode) {
        List<JmmNode> statements = new ArrayList<>();
        for (JmmNode child : methodNode.getChildren()) {
            if (child.getKind().equals("MethodStatement")) {
                if (child.getNumChildren() == 0) continue;
                statements.add(child.getJmmChild(0)); // MethodStatements have only 1 child: a statement
            }
        }
        return statements;
    }

    private List<JmmNode> getMethodIdentifierAccess(JmmNode node, String identifierName) {
        List<JmmNode> identifierNodes = new ArrayList<>();
        if (node.getKind().equals("Identifier") && node.get("value").equals(identifierName)) {
            identifierNodes.add(node);
            return identifierNodes;
        }
        for (JmmNode child : node.getChildren()) {
            identifierNodes.addAll(getMethodIdentifierAccess(child, identifierName));
        }
        return identifierNodes;
    }

    private List<JmmNode> getAssignmentStatements(JmmNode node) {
        List<JmmNode> assignments = new ArrayList<>();
        switch (node.getKind()) {
            case "Assignment":
                assignments.add(node);
            case "Scope":
                for (JmmNode child : node.getChildren()) {
                    assignments.addAll(getAssignmentStatements(child));
                }
                break;
            case "Conditional":
                assignments.addAll(getAssignmentStatements(node.getJmmChild(node.getNumChildren() - 1)));
                assignments.addAll(getAssignmentStatements(node.getJmmChild(node.getNumChildren() - 2)));
                break;
            default:
                break;
        }
        return assignments;
    }
}
