package pt.up.fe.comp2023.Analysers;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.SymbolTable;

import java.util.ArrayList;
import pt.up.fe.comp.jmm.ast.*;
import java.util.List;

public class ArrayAccessOverArray extends PreorderJmmVisitor<Integer, Integer> {
    private SymbolTable symbolTable;
    private List<Report> reports;

    public ArrayAccessOverArray(SymbolTable symbolTable, JmmNode root) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit("Expression", this::arrayAccessVisit);
        visit(root);
    }

    public Integer arrayAccessVisit(JmmNode node, Integer dummy) {

        String arrName = node.getJmmChild(0).get("array");
        var tempMethod = node.getJmmChild(0).getJmmParent().get("name");

        for (var localVariable : symbolTable.getLocalVariables(tempMethod)) {
            if (localVariable.getName().equals(arrName))
                if (!localVariable.getType().isArray()) {
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Var access have to be done over array"));
                } else {
                    return 0;
                }

        }

        for (var localParameter : symbolTable.getParameters(tempMethod)) {
            if (localParameter.getName().equals(arrName))
                if (!localParameter.getType().isArray()) {
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Var access have to be done over array"));
                } else {
                    return 0;
                }
        }

        var fields = symbolTable.getFields();

        for (var field : fields) {
            if (arrName.equals(field.getName())) {
                if (!field.getType().isArray()) {
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Var access have to be done over array"));
                } else {
                    return 0;
                }
            }
        }

        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Var access have to be done over array"));
        return 0;

    }

    @Override
    protected void buildVisitor() {

    }
}
