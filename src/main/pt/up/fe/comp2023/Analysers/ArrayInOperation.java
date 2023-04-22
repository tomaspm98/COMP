package pt.up.fe.comp2023.Analysers;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.report.StageResult;
import pt.up.fe.comp2023.SymbolTable;

import java.util.ArrayList;
import pt.up.fe.comp.jmm.ast.*;
import pt.up.fe.comp2023.visitors.SymbolTableVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayInOperation extends SymbolTableVisitor implements StageResult {
    /**
     * This class checks for *array_var* + 10
     * Since array_var is type int[] it should throw an error because array is not int.
     * This class stops at every BinOp and checks if each child is like the example.
     */

    private final SymbolTable symbolTable;
    private final List<Report>  reports;

    public ArrayInOperation(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        buildVisitor();
        addVisit("BinOpVisit", this::binOpVisit);
        //visit(rootNode);
    }

    public String binOpVisit(JmmNode node, String dummy) {
        List<JmmNode> children = new ArrayList<>();
        children.add(node.getJmmChild(0));
        children.add(node.getJmmChild(1));

        var tempMethod = node.getAncestor("MethodDeclaration");

        if (tempMethod != null) {
            String methodName = tempMethod.get().getJmmChild(0).get("value");
            for (JmmNode childNode : children) {
                // just checks for variables, since well-defined arrays have ArrayAccessExpression first
                if (!childNode.getKind().equals("Identifier"))
                    continue;

                if (isNodeIdArray(childNode, methodName, symbolTable)) {
                    if (node.get("op").equals("+") || node.get("op").equals("-") ||
                            node.get("op").equals("*") || node.get("op").equals("/")) {
                        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                                Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                                "Cannot use Arithmetic Operations with Arrays"));
                    }
                }
            }
        }
        return "";
    }

    private boolean isNodeIdArray(JmmNode childNode, String tempMethod, SymbolTable symbolTable) {
        String childName = childNode.getJmmChild(0).get("value");

        for (var localVariable : symbolTable.getLocalVariables(tempMethod)) {
            if (localVariable.getName().equals(childName)) {
                return localVariable.getType().isArray();
            }
        }

        for (var localParam : symbolTable.getParameters(tempMethod)) {
            if (localParam.getName().equals(childName)) {
                return localParam.getType().isArray();
            }
        }

        var fields = symbolTable.getFields();

        for (var f : fields) {
            if (childName.equals(f.getName())) {
                return f.getType().isArray();
            }
        }

        // default throws error since it didn't found the var name.
        return true;
    }

    @Override
    public List<Report> getReports() {
        return this.reports;
    }

    @Override
    public Map<String, String> getConfig() {
        return null;
    }
}
