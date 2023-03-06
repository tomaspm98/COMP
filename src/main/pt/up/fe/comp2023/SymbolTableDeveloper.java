package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

import java.util.LinkedHashMap;
import java.util.Map;

import java.util.List;

public class SymbolTableDeveloper extends AJmmVisitor<String, String> implements SymbolTable, Cloneable {
    private Map<String, Symbol> parameters;
    private Map<String, Symbol> variables;

    public SymbolTableDeveloper(){
        this.parameters= new LinkedHashMap<String, Symbol>();
        this.variables=new LinkedHashMap<String,Symbol>();
    }

   @Override
    protected void buildVisitor() {
        //addVisit("Imports", this::getImports);
        return;
    }
    @Override
    public List<String> getImports(){
        /*
        List<String> imports = new ArrayList<>();
        for (Symbol symbol : symbols) {
            if (symbol.isImported()) {
                imports.add(symbol.getImportName());
            }
        }
        return imports;

         */
        return null;
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public String getSuper() {
        return null;
    }

    @Override
    public List<Symbol> getFields() {
        return null;
    }

    @Override
    public List<String> getMethods() {
        return null;
    }

    @Override
    public Type getReturnType(String s) {
        return null;
    }

    @Override
    public List<Symbol> getParameters(String s) {
        return (List<Symbol>) this.parameters;
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SymbolTable clone() throws CloneNotSupportedException{
        SymbolTable newTable = (SymbolTable)super.clone();
        //newTable.ge = (Map<String, Symbol>)this.variables.clone();
        return newTable;
    }
}
