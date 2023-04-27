package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.StageResult;


import java.util.ArrayList;
import java.util.List;


public abstract class SemanticAnalyser extends PreorderJmmVisitor<Integer, Integer> implements StageResult {
    private List<Report> reports;
    //private SymbolTable symboltable;

    public SemanticAnalyser(){
        this.reports=new ArrayList<>();
    }

    @Override
    public List<Report> getReports(){
        return reports;
    }

    protected void addReport(Report report){
        reports.add(report);
    }

  /*  private Symbol getDeclaredSymbol(String name, String methodName){
        Symbol returnSymb = null;

        List<Symbol> localVariables = symboltable.getLocalVariables(methodName);
        for (int i = 0;i<localVariables.size();i++){
            if (localVariables.get(i).getName()==name){
                returnSymb=localVariables.get(i);
            }
        }

        if (returnSymb==null){
            List<Symbol> fields = symboltable.getFields();
            for(int j = 0;j<fields.size();j++) {
                if (fields.get(j).getName() == name){
                    returnSymb = fields.get(j);
                }
            }
        }

        if (returnSymb==null){
            List<Symbol> params = symboltable.getParameters(methodName);
            for (int h=0;h<params.size();h++){
                if (params.get(h).getName() == name){
                    returnSymb =  params.get(h);
                }
            }
        }


        return returnSymb;


    }


    public List<Report> getReports(){
        return reports;
    };


    @Override
    protected void buildVisitor() {

    }*/
}



