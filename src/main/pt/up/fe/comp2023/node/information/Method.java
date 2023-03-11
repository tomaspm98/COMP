package pt.up.fe.comp2023.node.information;


import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.List;

public class Method {
    private String name;
    private Type retType;
    private List<Symbol> parameters;
    private List<Symbol> variables;

    public Method() {
        this.name = new String();
        this.parameters = new ArrayList<>();
    }
    public Method(String name, String retType) {
        this.name = name;
        this.retType = new Type(name, retType.equals("IntArray"));
        this.variables = new ArrayList<>();
        this.parameters = new ArrayList<>();
    }

    public Method(String name, Type retType) {
        this.name = name;
        this.retType = retType;
        this.variables = new ArrayList<>();
        this.parameters = new ArrayList<>();
    }
    public boolean addParameter(Symbol parameter) {
        return this.parameters.add(parameter);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getRetType() {
        return retType;
    }

    public void setRetType(String retType) {
        this.retType = new Type(retType, retType.equals("IntArray"));
    }

    public List<Symbol> getVariables() {
        return variables;
    }

    public void setVariables(List<Symbol> variables) {
        this.variables = variables;
    }

    public boolean addVariable(Symbol variable) {return this.variables.add(variable);}

    public List<Symbol> getParameters(){ return parameters;}

    public void setParameters(List<Symbol> parameters) { this.parameters = parameters; }

}
