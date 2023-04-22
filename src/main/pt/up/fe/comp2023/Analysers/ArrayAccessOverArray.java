package pt.up.fe.comp2023.Analysers;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.report.StageResult;
import pt.up.fe.comp2023.SymbolTable;

import java.util.ArrayList;
import pt.up.fe.comp.jmm.ast.*;
import pt.up.fe.comp2023.visitors.SymbolTableVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayAccessOverArray extends SymbolTableVisitor implements StageResult{
    private SymbolTable symbolTable;
    private List<Report> reports;

    public ArrayAccessOverArray(SymbolTable symbolTable, JmmNode root) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        buildVisitor();
        //addVisit("Assignment", this::arrayAccessVisit);
        addVisit("Array", this::addVisit);
        visit(root);
    }

    public String arrayAccessVisit(JmmNode node, String dummy) {

        JmmNode arrayNode = node.getJmmChild(0);
        if (arrayNode.getKind().equals("Identifier")) {
            String arrName = arrayNode.get("value");
            var tempMethod = node.getAncestor("MethodDeclaration");

            if (tempMethod != null) {
                String methodName = tempMethod.get().getJmmChild(0).get("value");

                List<Symbol> localVariables = symbolTable.getLocalVariables(methodName);
                if (localVariables != null) {
                    for (var localVariable : localVariables)
                    if (localVariable.getName().equals(arrName)) {
                        if (!localVariable.getType().isArray()) {
                            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Var access have to be done over array"));
                        } else {
                            return " ";
                        }
                    }
                }

                List<Symbol> localParameters = symbolTable.getParameters(methodName);
                if (localParameters != null) {
                    for (var localParameter : localParameters) {
                        if (localParameter.getName().equals(arrName)) {
                            if (!localParameter.getType().isArray()) {
                                this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Var access have to be done over array"));
                            } else {
                                return " ";
                            }
                        }
                    }
                }
            }

            var fields = symbolTable.getFields();

            if (fields != null) {
                for (var field : fields) {
                    if (arrName.equals(field.getName())) {
                        if (!field.getType().isArray()) {
                            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Var access have to be done over array"));
                        } else {
                            return " ";
                        }
                    }
                }

            }

            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Var access have to be done over array"));
        }

        return " ";
    }

    public String addVisit(JmmNode node, String dummy) {
        JmmNode leftOperand = node.getJmmChild(0);
        JmmNode rightOperand = node.getJmmChild(1);

        if (isOperandArray(leftOperand) || isOperandArray(rightOperand)) {
            reportIncompatibleTypes(node);
        }

        return "";
    }

    private boolean isOperandArray(JmmNode operand) {
        if (operand.getKind().equals("Identifier")) {
            String varName = operand.get("value");
            var tempMethod = operand.getAncestor("MethodDeclaration");
            if (tempMethod != null) {
                String methodName = tempMethod.get().getJmmChild(0).get("value");

                for (var localVariable : symbolTable.getLocalVariables(methodName)) {
                    if (localVariable.getName().equals(varName)) {
                        return localVariable.getType().isArray();
                    }
                }

                for (var localParameter : symbolTable.getParameters(methodName)) {
                    if (localParameter.getName().equals(varName)) {
                        return localParameter.getType().isArray();
                    }
                }
            }

            var fields = symbolTable.getFields();

            for (var field : fields) {
                if (varName.equals(field.getName())) {
                    return field.getType().isArray();
                }
            }
        } else if (operand.getKind().equals("NewArray")) {
            return true;
        }

        return false;
    }
    private void reportIncompatibleTypes(JmmNode node) {
        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Incompatible types: cannot add an array and an integer"));
    }

    @Override
    public Map<String, String> getConfig() {
        return new HashMap<>();
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }
}
