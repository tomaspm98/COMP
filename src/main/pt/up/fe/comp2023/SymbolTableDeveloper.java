package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.List;

public class SymbolTableDeveloper implements SymbolTable {
    private Map<String, Symbol> parameters;
    private Map<String, Symbol> variables;
    private List<String> imports;
    private List<Symbol> fields;
    private String className;
    private String superClass;


    public SymbolTableDeveloper(){
        this.parameters= new LinkedHashMap<String, Symbol>();
        this.variables=new LinkedHashMap<String,Symbol>();
        this.imports = new ArrayList<>();
        this.className= new String();
    }

    @Override
    public List<String> getImports(){
        return imports;
    }

    public void addImport(String s) {
        imports.add(s);
    }

    @Override
    public String getClassName() {
        return className;
    }
    public void setClassName(String className){
        this.className=className;
    }

    @Override
    public String getSuper() {
        return superClass;
    }
    public void setSuper(String superClass){
        this.superClass=superClass;
    }

    @Override
    public List<Symbol> getFields() {
        return fields;
    }

    @Override
    public List<String> getMethods() {return null;}

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

}
