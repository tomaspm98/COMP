package pt.up.fe.comp2023.Analysers;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.report.StageResult;
import pt.up.fe.comp2023.SymbolTable;
import pt.up.fe.comp2023.visitors.SymbolTableVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeOperation extends SymbolTableVisitor implements StageResult{

    private SymbolTable symbolTable;
    private List<Report> reports;

    public TypeOperation(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        buildVisitor();
        addVisit("Operation", this::operationVisit);
        visit(rootNode);
    }
    public String operationVisit(JmmNode node, String dummy) {
        String nodeValue = node.get("op");

        String res = _typeCheck(node);

        if ("null".equals(res)) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                    "Operation with wrong types"));
        }

        return "";
    }

    public String getIdType(JmmNode node){
        JmmNode parentNode = node;
        while (parentNode != null && !parentNode.getKind().equals("MethodDeclaration")) {
            parentNode = parentNode.getJmmParent();
        }

        if (parentNode == null) {
            return "";
        }

        String parentMethodName = parentNode.get("name");

        // Local Variables
        for (var localVariable : symbolTable.getLocalVariables(parentMethodName)) {
            if (node.get("name").equals(localVariable.getName())) {
                return localVariable.getType().getName();
            }
        }

        // Parameters
        for (var param : symbolTable.getParameters(parentMethodName)) {
            if (node.get("name").equals(param.getName())) {
                return param.getType().getName();
            }
        }

        // Fields
        for (var field : symbolTable.getFields()) {
            if (node.get("name").equals(field.getName())) {
                return field.getType().getName();
            }
        }
        return "";
    }

    private String _typeCheck(JmmNode node) {
        var myKind = node.getKind();

        if (myKind.equals("BoolBinaryOp")) {
            boolean isAnd = node.get("op").equals("&&");
            var left = _typeCheck(node.getChildren().get(0));
            var right = _typeCheck(node.getChildren().get(1));
            if (isAnd && !(left.equals("boolean") && right.equals("boolean"))) {
                return "null";
            }
            if (!(left.equals("int")) && right.equals("int")) {
                return "null";
            }

            if (node.get("op").equals("&&") || node.get("op").equals("<")) {
                return "boolean";
            }
            if (left.equals("int") && right.equals("int")) {
                return "int";
            }
        }

        if (myKind.equals("Integer")) {
            return "int";
        }
        if (myKind.equals("Identifier")) {
            return getIdType(node);
        }

        if (myKind.equals("MethodDeclaration")) {
            return "null";
        }

        if (myKind.equals("Boolean")) {
            return "boolean";
        }

        return "null";
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }

    @Override
    public Map<String, String> getConfig() {
        return new HashMap<>();
    }
}
