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
    private final SymbolTable symbolTable;
    private final List<Report> reports;

    public ArrayAccessOverArray(SymbolTable symbolTable, JmmNode root) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        //buildVisitor();
        addVisit("ArrayAccessExpression", this::arrayAccessVisit);
        visit(root);
    }

    public String arrayAccessVisit(JmmNode node, String dummy) {
        String arrName = node.getJmmChild(0).get("name");

        var tempMethod = node.getJmmParent().get("name");

        // for each local variable
        for (var localVariable :symbolTable.getLocalVariables(tempMethod)) {
            if(localVariable.getName().equals(arrName))
                if(!localVariable.getType().isArray()){
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                            Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                            "Var access must be done over array"));
                }else{
                    return "";
                }

        }

        // for each method parameter
        for(var localParam : symbolTable.getParameters(tempMethod)){
            if(localParam.getName().equals(arrName))
                if(!localParam.getType().isArray()){
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                            Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                            "Var access must be done over array"));
                }else{
                    return "";
                }
        }


        var fields = symbolTable.getFields();

        // for each class field
        for (var f:fields )
            if(arrName.equals(f.getName())){
                if(!f.getType().isArray()){
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                            Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                            "Var access must be done over array"));
                }else{
                    return "";
                }
            }


        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                "Var access must be done over array"));
        return "";
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
