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

public class InvalidArrayOperations extends SymbolTableVisitor {
    private SymbolTable symbolTable;
    private List<Report> reports;

    public InvalidArrayOperations(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        buildVisitor();
        addVisit("Add", this::addVisit);
        visit(rootNode);
    }

    public String addVisit(JmmNode node, String dummy) {
        JmmNode leftNode = node.getJmmChild(0);
        JmmNode rightNode = node.getJmmChild(1);

        if (isArrayNode(leftNode) && isIntegerNode(rightNode) || isIntegerNode(leftNode) && isArrayNode(rightNode)) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    "Invalid operation: Adding an array and an integer"));
        }

        return "";
    }

    private boolean isArrayNode(JmmNode node) {
        if (node.getKind().equals("Identifier")) {
            String name = node.get("name");

            Optional<JmmNode> tempMethodOpt = node.getAncestor("methodDeclaration");
            if (tempMethodOpt.isPresent()) {
                JmmNode tempMethod = tempMethodOpt.get();
                String methodName = tempMethod.get("name");

                for (var localVar : symbolTable.getLocalVariables(methodName)) {
                    if (localVar.getName().equals(name) && localVar.getType().isArray()) {
                        return true;
                    }
                }

                for (var param : symbolTable.getParameters(methodName)) {
                    if (param.getName().equals(name) && param.getType().isArray()) {
                        return true;
                    }
                }
            }

            for (var field : symbolTable.getFields()) {
                if (field.getName().equals(name) && field.getType().isArray()) {
                    return true;
                }
            }
        } else if (node.getKind().equals("NewIntArray")) {
            return true;
        }
        return false;
    }


    private boolean isIntegerNode(JmmNode node) {
        return node.getKind().equals("Integer");
    }

    public List<Report> getReports() {
        return reports;
    }
}

