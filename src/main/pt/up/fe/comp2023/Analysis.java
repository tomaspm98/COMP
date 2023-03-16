package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp2023.visitors.SymbolTableVisitor;

import java.util.ArrayList;

public class Analysis implements JmmAnalysis {

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        SymbolTable table = new SymbolTable();
        SymbolTableVisitor visitor = new SymbolTableVisitor(table);
        visitor.visit(jmmParserResult.getRootNode());
        return new JmmSemanticsResult(jmmParserResult, table, new ArrayList<>());
    }
}
