package pt.up.fe.comp2023.Analysers;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.report.StageResult;
import pt.up.fe.comp2023.SymbolTable;
import pt.up.fe.comp2023.visitors.SymbolTableVisitor;

import java.util.*;

public class VarNotDeclared extends SymbolTableVisitor implements StageResult {
    private SymbolTable symbolTable;
    private List<Report> reports;

    public VarNotDeclared(SymbolTable symbolTable, JmmNode rootNode) {
        super(symbolTable);
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        buildVisitor();
        addVisit("Variable", this::idVisit);
        visit(rootNode);
    }

    public String idVisit(JmmNode node, String dummy) {
        var father = node.getJmmParent();

        if (father.getKind().equals("importDeclaration")
                || father.getKind().equals("varDeclaration")
                || father.getKind().equals("fieldDeclaration")) {
            return "";
        }

        if (father.getKind().equals("MethodCall")) {
            var firstChild = father.getJmmChild(0);

            if (firstChild.getKind().equals("Integer") || firstChild.getKind().equals("Boolean")) {
                if (symbolTable.getMethods().contains(father.getJmmChild(1).get("name")))
                    return "";
                else {
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                            "Var is not declared"));
                    return "";
                }
            }

            if (search(firstChild.get("name"), node)) {
                return "";
            }

        } else {
            if (search(node.get("name"), node)) {
                return "";
            }
        }

        if (symbolTable.getClassName().equals(node.get("name")))
            return "";

        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                "Var is not declared"));

        return "";
    }

    public boolean search(String childName, JmmNode node) {
        Optional<JmmNode> tempMethodOpt = node.getAncestor("methodDeclaration");

        if (tempMethodOpt.isPresent()) {
            JmmNode tempMethod = tempMethodOpt.get();
            String methodName = tempMethod.get("name");

            for (var localVar : symbolTable.getLocalVariables(methodName)) {
                if (localVar.getName().equals(childName)) {
                    return true;
                }
            }

            for (var param : symbolTable.getParameters(methodName)) {
                if (param.getName().equals(childName)) {
                    return true;
                }
            }
        }

        for (var field : symbolTable.getFields()) {
            if (field.getName().equals(childName)) {
                return true;
            }
        }

        for (var imports : symbolTable.getImports()) {
            if (childName.equals(imports)) {
                return true;
            }
        }

        return false;
    }

    public List<Report> getReports() {
        return reports;
    }

    @Override
    public Map<String, String> getConfig() {
        return new HashMap<>();
    }


}
