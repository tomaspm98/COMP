package pt.up.fe.comp2023.Analysers;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.report.StageResult;
import pt.up.fe.comp2023.SemanticAnalyser;
import pt.up.fe.comp2023.SymbolTable;

import java.util.ArrayList;
import pt.up.fe.comp.jmm.ast.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class MethodCallEqualsMethodDeclaration extends PreorderJmmVisitor<Integer, Integer> implements StageResult {
    private final SymbolTable symbolTable;
    private final List<Report> reports;

    public MethodCallEqualsMethodDeclaration(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit("MethodCall", this::callExpressionVisit);
        visit(rootNode);
    }

    Integer callExpressionVisit(JmmNode node, Integer dummy) {
        boolean exists = exists(node);
        if (!exists) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                    "Method doesn't exist."));
        }

        return 0;
    }

    private boolean exists(JmmNode node) {
        var base = node.getJmmChild(0);
        var method = node.getJmmChild(1);
        var type = getIdType(base);

        // verificar class / extend / imports
        if (type.equals("null")) {
            if (base.get("name").equals(symbolTable.getClassName())) {
                if (symbolTable.getSuper() == null) {
                    return symbolTable.getMethods().contains(method.get("name"));
                } else {
                    return true;
                }
            }
            if (base.get("name").equals(symbolTable.getSuper())) {
                return true;
            }
            if (symbolTable.getImports().contains(base.get("name"))) return true;

        }
        if (type.equals(symbolTable.getClassName())) {
            if (symbolTable.getSuper() == null) {
                return symbolTable.getMethods().contains(method.get("name"));
            } else {
                return true;
            }
        }
        if (type.equals(symbolTable.getSuper())) {
            return true;
        }
        if (symbolTable.getImports().contains(type)) return true;

        return false;
    }

    public String getIdType(JmmNode node){
        var currentNode = node;
        var father=node;

        while(!currentNode.getKind().equals("methodDeclaration") &&
                !currentNode.getKind().equals("Program")){
            currentNode = currentNode.getJmmParent();

        }
        if(!currentNode.getKind().equals("Program")) {
            father = currentNode;
        }
        else{ father = null; }

        //localVars
        for (var localVariable :symbolTable.getLocalVariables( father.get("name") )) {
            if(node.get("name").equals(localVariable.getName()))
                return localVariable.getType().getName();
        }
        //params
        for (var param :symbolTable.getParameters( father.get("name") )) {
            if(node.get("name").equals(param.getName()))
                return param.getType().getName();
        }
        //fields
        for (var field :symbolTable.getFields() ) {
            if(node.get("name").equals(field.getName()))
                return field.getType().getName();
        }
        return "null";
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }

    @Override
    public Map<String, String> getConfig() {
        return new HashMap<String, String>();
    }

    @Override
    protected void buildVisitor() {

    }
}
