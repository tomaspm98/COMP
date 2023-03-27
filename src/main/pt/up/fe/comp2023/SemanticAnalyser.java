package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;


import java.util.ArrayList;
import java.util.List;


public class SemanticAnalyser extends PreorderJmmVisitor<Boolean, Boolean> {
    private List<Report> reports;
    private SymbolTable symboltable;

    public SemanticAnalyser(SymbolTable symboltable){
        this.reports=new ArrayList<>();
        this.symboltable=symboltable;
    }

    private Symbol getDeclaredSymbol(String name, String methodName){
        Symbol returnSymb = null;

        List<Symbol> localVariables = symboltable.getLocalVariables(methodName);
        for (int i = 0;i<localVariables.size();i++){
            if (localVariables.get(i).getName()==name){
                returnSymb=localVariables.get(i);
            }
        }

        if (returnSymb==null){
            List<Symbol> fields = symboltable.getFields();
            for (int j = 0;j<fields.size();j++){
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

        if (returnSymb == null){
            List<String> imports = symboltable.getImports();
            for (int k=0;k<imports.size();k++){
                if (imports.get(k)==name){
                    //returnSymb = (imports.get(k)).substring((imports.get(k)).lastIndexOf('.') + 1); //TODO converter para symbol
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

    }
}
