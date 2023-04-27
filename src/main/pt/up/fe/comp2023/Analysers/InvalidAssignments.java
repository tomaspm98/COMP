package pt.up.fe.comp2023.Analysers;


import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.SymbolTable;
import pt.up.fe.comp2023.visitors.SymbolTableVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvalidAssignments extends SymbolTableVisitor {
    private List<Report> reports;
    private SymbolTable symbolTable;

    public InvalidAssignments(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        buildVisitor();
        addVisit("Assignment", this::assignmentVisit);
        visit(rootNode);
    }

    public String assignmentVisit(JmmNode node, String dummy) {
        JmmNode leftNode = node.getJmmChild(0);
        JmmNode rightNode = null;

        for (JmmNode child : node.getChildren()) {
            if (child != leftNode) {
                rightNode = child;
                break;
            }
        }

        if (leftNode.getKind().equals("Identifier")) {
            String varName = leftNode.get("value");
            String varType = null;

            Optional<JmmNode> tempMethodOpt = leftNode.getAncestor("methodDeclaration");
            if (tempMethodOpt.isPresent()) {
                JmmNode tempMethod = tempMethodOpt.get();
                String methodName = tempMethod.get("name");

                // Check if the variable is a local variable
                for (var localVar : symbolTable.getLocalVariables(methodName)) {
                    if (localVar.getName().equals(varName)) {
                        varType = localVar.getType().toString();
                        break;
                    }
                }

                // Check if the variable is a parameter
                if (varType == null) {
                    for (var param : symbolTable.getParameters(methodName)) {
                        if (param.getName().equals(varName)) {
                            varType = param.getType().toString();
                            break;
                        }
                    }
                }
            }

            // Check if the variable is a field
            if (varType == null) {
                for (var field : symbolTable.getFields()) {
                    if (field.getName().equals(varName)) {
                        varType = field.getType().toString();
                        break;
                    }
                }
            }

            if (varType != null && varType.equals("boolean")) {
                if (rightNode.getKind().equals("Integer")) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                            "Cannot assign an int value to a boolean variable"));
                }
            }
        }

        return "";
    }



    public List<Report> getReports() {
        return reports;
    }
}

