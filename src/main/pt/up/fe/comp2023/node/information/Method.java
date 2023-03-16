package pt.up.fe.comp2023.node.information;


import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.specs.util.collections.SpecsList;

import java.util.List;

public class Method {

    private SpecsList<String> modifiers;
    private String name;
    private Type retType;
    private SpecsList<Symbol> arguments;
    private SpecsList<Symbol> variables;

    public Method() {
        this.name = "";
        this.modifiers = SpecsList.newInstance(String.class);
        this.arguments = SpecsList.newInstance(Symbol.class);
        this.variables = SpecsList.newInstance(Symbol.class);
    }

    public Method(String name) {
        this.name = name;
        this.modifiers = SpecsList.newInstance(String.class);
        this.arguments = SpecsList.newInstance(Symbol.class);
        this.variables = SpecsList.newInstance(Symbol.class);
    }

    public Method(String name, String retType) {
        this.name = name;
        this.modifiers = SpecsList.newInstance(String.class);
        this.retType = new Type(name, retType.equals("IntArray"));
        this.arguments = SpecsList.newInstance(Symbol.class);
        this.variables = SpecsList.newInstance(Symbol.class);
    }

    public Method(String name, Type retType) {
        this.name = name;
        this.modifiers = SpecsList.newInstance(String.class);
        this.retType = retType;
        this.arguments = SpecsList.newInstance(Symbol.class);
        this.variables = SpecsList.newInstance(Symbol.class);
    }
    public boolean addArgument(Symbol argument) {
        return this.arguments.add(argument);
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

    public void setRetType(Type retType) {
        this.retType = retType;
    }

    public List<Symbol> getVariables() {
        return variables;
    }

    public void setVariables(List<Symbol> variables) {
        this.variables = SpecsList.newInstance(Symbol.class);
        this.variables.addAll(variables);
    }

    public void setVariables(SpecsList<Symbol> variables) {
        this.variables = variables;
    }

    public boolean addVariable(Symbol variable) {return this.variables.add(variable);}

    public List<Symbol> getArguments(){ return arguments;}

    public void setArguments(List<Symbol> arguments) {
        this.arguments = SpecsList.newInstance(Symbol.class);
        this.arguments.addAll(arguments);
    }
    public void setArguments(SpecsList<Symbol> arguments) { this.arguments = arguments; }

    public void addModifier(String newModifier) {
        this.modifiers.add((newModifier));
    }

    public SpecsList<String> getModifiers() {
        return this.modifiers;
    }
}
