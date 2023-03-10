package pt.up.fe.comp2023;

import java.util.List;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class SymbolTableVisitor extends AJmmVisitor<String, String> {
    private final SymbolTableDeveloper table;

    @Override
    protected void buildVisitor() {
        addVisit("ImportDeclaration", this::dealWithImport);
        addVisit("NextImport", this::dealWithNextImport);
    }

    public SymbolTableVisitor(SymbolTableDeveloper table) {
        this.table = table;
    }

    private String dealWithImport(JmmNode node, String s) {
        table.addImport(node.get("value"));
        return s + "import" + node.get("value") + ";";
    }

    private String dealWithNextImport(JmmNode node, String s) {
        List<String> imports = table.getImports();
        String lastImport = imports.get(imports.size() - 1);
        String nextImport = lastImport + '.' + node.get("value");
        imports.set(imports.size() - 1, nextImport);

        return s + nextImport + ";";
    }
}
