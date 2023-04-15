package pt.up.fe.comp2023.Analysers;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayAccessOverArray extends PreorderJmmVisitor<Integer, Integer> implements StageResult {
    private SymbolTable symbolTable;
    private List<Report> reports;

    public ArrayAccessOverArray(SymbolTable symbolTable, JmmNode root) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit("ArrayAccess", this::arrayAccessVisit);
        visit(root);
    }

    public Integer arrayAccessVisit(JmmNode node, Integer dummy) {

        JmmNode arrayNode = node.getJmmChild(0);
        if (arrayNode.getKind().equals("Identifier")) {
            String arrName = arrayNode.get("value");
            var tempMethod = node.getAncestor("MethodDeclaration");

            if (tempMethod != null) {
                String methodName = tempMethod.get().getJmmChild(0).get("value");

                for (var localVariable : symbolTable.getLocalVariables(methodName)) {
                    if (localVariable.getName().equals(arrName)) {
                        if (!localVariable.getType().isArray()) {
                            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Var access have to be done over array"));
                        } else {
                            return 0;
                        }
                    }
                }

                for (var localParameter : symbolTable.getParameters(methodName)) {
                    if (localParameter.getName().equals(arrName)) {
                        if (!localParameter.getType().isArray()) {
                            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Var access have to be done over array"));
                        } else {
                            return 0;
                        }
                    }
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
        }

        return 0;
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }

    @Override
    public Map<String, String> getConfig() {
        return new HashMap<String, String>();
    }

    @Override
    protected void buildVisitor(){

    }


}
