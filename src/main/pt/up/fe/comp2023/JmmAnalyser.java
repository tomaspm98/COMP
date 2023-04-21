package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.report.StageResult;
import pt.up.fe.comp2023.Analysers.*;
import pt.up.fe.comp2023.visitors.SymbolTableVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JmmAnalyser implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        List<Report> reports = new ArrayList<>();

        var symbolTable = new SymbolTable();

        var symbolTableFiller = new SymbolTableVisitor(symbolTable);

        symbolTableFiller.visit(parserResult.getRootNode()); // Fills the information for symbolTable

        reports.addAll(symbolTableFiller.getReports());

        List<SymbolTableVisitor> analysers = Arrays.asList(
                new ArrayAccessOverArray(symbolTable, parserResult.getRootNode()),
                new VarNotDeclared(symbolTable, parserResult.getRootNode()),
                new MethodCallEqualsMethodDeclaration(symbolTable, parserResult.getRootNode()),
                new AssignType(symbolTable, parserResult.getRootNode()),
                new TypeOperation(symbolTable, parserResult.getRootNode()),
                new ArrayInOperation(symbolTable, parserResult.getRootNode())
        );

        for(var analyser : analysers){
            //analyser.visit(parserResult.getRootNode());
            reports.addAll(analyser.getReports());
        }

        //reports.addAll(symbolTableFiller.getReports());

        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}

