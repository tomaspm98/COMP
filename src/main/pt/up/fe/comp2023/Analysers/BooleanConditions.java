package pt.up.fe.comp2023.Analysers;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.report.StageResult;
import pt.up.fe.comp2023.SymbolTable;
import pt.up.fe.comp2023.visitors.SymbolTableVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BooleanConditions extends SymbolTableVisitor implements StageResult{

    private SymbolTable symbolTable;
    private List<Report> reports;

    public BooleanConditions(SymbolTable symbolTable, JmmNode rootNode) {
        super(symbolTable);
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        buildVisitor();
        addVisit("Condition", this::checkConditionExpression);
        visit(rootNode);
    }

    public String checkConditionExpression(JmmNode node, String dummy) {
        JmmNode conditionNode = node.getChildren().get(0);
        String expressionType = getExpressionType(conditionNode);

        if (!expressionType.equals("boolean")) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    Integer.valueOf(-1), Integer.valueOf(-1),
                    "Expression in the condition must return a boolean value"));
        }
        return "";
    }

    private String getExpressionType(JmmNode node) {
        String nodeKind = node.getKind();

        if (nodeKind.equals("BoolBinaryOp")) {
            return "boolean";
        }

        if (nodeKind.equals("Identifier")) {
            return getIdType(node);
        }

        if (nodeKind.equals("UnaryBinaryOp")) {
            return getExpressionType(node.getChildren().get(0));
        }

        // TODO: Add more cases for other node types if necessary

        return "unknown";
    }

    private String getIdType(JmmNode node) {
        JmmNode current = node.getJmmParent();
        while (current != null) {
            String kind = current.getKind();
            if (kind.equals("methodDeclaration")) {
                // LocalVars
                for (var localVariable : symbolTable.getLocalVariables(current.get("name"))) {
                    if (node.get("name").equals(localVariable.getName()))
                        return localVariable.getType().getName();
                }
                // Params
                for (var param : symbolTable.getParameters(current.get("name"))) {
                    if (node.get("name").equals(param.getName()))
                        return param.getType().getName();
                }
                // Fields
                for (var field : symbolTable.getFields()) {
                    if (node.get("name").equals(field.getName()))
                        return field.getType().getName();
                }
                break;
            }
            current = current.getJmmParent();
        }
        return "";
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }

    @Override
    public Map<String, String> getConfig() {
        return null;
    }
}

