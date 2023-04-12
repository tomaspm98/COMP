package pt.up.fe.comp2023;

import jdk.javadoc.doclet.Reporter;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.visitors.OllirGenerator;
import pt.up.fe.specs.util.collections.SpecsList;

public class Optimizer implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        String ollirCode = "";
        SpecsList<Report> reports = SpecsList.newInstance(Report.class);
        JmmNode root = jmmSemanticsResult.getRootNode();
        OllirGenerator generator = new OllirGenerator();
        return new OllirResult(jmmSemanticsResult, ollirCode, reports);;
    }
}
