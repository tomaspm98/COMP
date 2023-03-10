package pt.up.fe.comp2023.node.information;


import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.ArrayList;
import java.util.List;

public class Method {
    private String name;
    private String retType;
    private List<Symbol> parameters;
    public Method() {
        this.name = new String();
        this.retType = new String();
        this.parameters = new ArrayList<>();
    }
    public Method(String name, String retType) {
        this.name = name;
        this.retType = retType;
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

    public String getRetType() {
        return retType;
    }

    public void setRetType(String retType) {
        this.retType = retType;
    }
}
