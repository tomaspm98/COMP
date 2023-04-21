package pt.up.fe.comp2023.utils;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

public class SymbolInfo {
    private Symbol symbol;
    private SymbolPosition symbolPosition;

    public SymbolInfo(Symbol symbol, SymbolPosition symbolPosition) {
        this.symbol = symbol;
        this.symbolPosition = symbolPosition;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public SymbolPosition getSymbolPosition() {
        return symbolPosition;
    }

    public void setSymbolPosition(SymbolPosition symbolPosition) {
        this.symbolPosition = symbolPosition;
    }
}
