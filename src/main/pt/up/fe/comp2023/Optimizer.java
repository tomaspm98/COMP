package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.visitors.OllirGenerator;
import pt.up.fe.specs.util.collections.SpecsList;

import java.util.List;

public class Optimizer implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        String ollirCode = "";
        SpecsList<Report> reports = (SpecsList<Report>) jmmSemanticsResult.getReports();
        JmmNode root = jmmSemanticsResult.getRootNode();
        OllirGenerator generator = new OllirGenerator((SymbolTable) jmmSemanticsResult.getSymbolTable(), reports);
        return new OllirResult(jmmSemanticsResult, ollirCode, reports);
    }
}
