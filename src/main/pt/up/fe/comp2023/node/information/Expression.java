package pt.up.fe.comp2023.node.information;

import pt.up.fe.comp.jmm.analysis.table.Type;

public class Expression {

    private Type retType;

    public Expression() {
        this.retType = null;
    }
    public Expression(Type retType) {
        this.retType = retType;
    }
    public boolean isCondition() {
        return true; //TODO implement
    };

    public Type getRetType() {
        return this.retType;
    }
}
