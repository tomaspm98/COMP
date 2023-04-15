package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.StageResult;
import pt.up.fe.comp2023.Analysers.ArrayAccessOverArray;
import pt.up.fe.comp2023.Analysers.MethodCallEqualsMethodDeclaration;
import pt.up.fe.comp2023.Analysers.VarNotDeclared;
import pt.up.fe.comp2023.visitors.SymbolTableVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JmmAnalyser implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        List<Report> reports = new ArrayList<>();

        var symbolTable = new SymbolTable();

        var symbolTableFiller = new SymbolTableVisitor();

        symbolTableFiller.visit(parserResult.getRootNode(), String.valueOf(symbolTable)); // Fills the information for symbolTable

        reports.addAll(symbolTableFiller.getReports());

        List<StageResult> analysers = Arrays.asList(
                new ArrayAccessOverArray(symbolTable, parserResult.getRootNode()),
                new VarNotDeclared(symbolTable, parserResult.getRootNode()),
                new MethodCallEqualsMethodDeclaration(symbolTable, parserResult.getRootNode())
        );

        for(var analyser : analysers){
            reports.addAll(analyser.getReports());
        }

        return new JmmSemanticsResult(parserResult, symbolTable,reports);
    }
}

