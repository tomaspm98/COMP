package pt.up.fe.comp2023.node.information;

import pt.up.fe.comp.jmm.analysis.table.Type;

public class Expression {

    private Type retType; // TODO maybe we don't want a type - ENUM instead? Check consequences in SymbolTableVisitor::isInScope

    public boolean isCondition() {
        return true; //TODO implement
    };

    public Type retType() {
        return this.retType;
    }
}
