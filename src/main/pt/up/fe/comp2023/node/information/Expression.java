package pt.up.fe.comp2023.node.information;

import pt.up.fe.comp.jmm.analysis.table.Type;

public class Expression {

    private Type retType;
    private boolean isIdentifier;

    public Expression() {
        this.retType = new Type("temp", false); // temporary fix until this is needed
    }
    public Expression(Type retType) {
        this.retType = retType;
    }

    public boolean isCondition() { // TODO remove, use isBool instead
        return this.retType.equals(new Type("boolean", false));
    };

    public Type getRetType() {
        return this.retType;
    }

    public void setRetType(Type type) { this.retType = type; }

    public boolean isInt() {
        return this.retType.equals(new Type("int", false));
    }

    public boolean isBool() {
        return this.retType.equals(new Type("boolean", false));
    }

    public boolean isIdentifier() {
        return this.isIdentifier;
    }

    public boolean isArray() {
        return this.retType.isArray();
    }
}
