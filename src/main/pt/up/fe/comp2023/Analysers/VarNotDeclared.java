package pt.up.fe.comp2023.Analysers;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.SymbolTable;

import java.util.ArrayList;
import pt.up.fe.comp.jmm.ast.*;
import java.util.List;

public class VarNotDeclared extends PreorderJmmVisitor<Integer, Integer>{
    private SymbolTable symbolTable;
    private List<Report>  reports;

    public VarNotDeclared(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit("Identifier", this::idVisit);
        visit(rootNode);
    }
    public Integer idVisit(JmmNode node, Integer dummy) {

        var father = node.getJmmParent();

        if(father.getKind().equals("ImportDeclaration")
                || father.getKind().equals("VarDeclaration")
                || father.getKind().equals("FieldDeclaration")  ){
            return 0;
        }
        if(father.getKind().equals("MethodCall")){
            var firstChild =father.getJmmChild(0);

            if(firstChild.getKind().equals("Integer") || firstChild.getKind().equals("Boolean")){
                if(symbolTable.getMethods().contains(father.getJmmChild(1).get("name")))
                    return 0;
                else{
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                            "Var is not declared"));
                    return 0;
                }

            }

            if(search(firstChild.get("name"), node)){
                return 0;
            }

        }else{
            if(search(node.get("name"), node)){
                return 0;
            }
        }
        if(symbolTable.getClassName().equals(node.get("name")))
            return 0;

        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                "Var is not declared"));

        return 0;
    }



    public boolean search( String childName , JmmNode node){
        //var tempMethod = AstUtils.getPreviousNode(node, AstNode.METHOD_DECLARATION).get("name");

        var currentNode = node;
        while(!currentNode.getKind().equals("Method Declaration") && !currentNode.getKind().equals("Program")){
            currentNode=currentNode.getJmmParent();
        }
        if (currentNode.getKind().equals("Program")){
            return false;
        }

        var tempMethod = currentNode.get("name");

        for (var localVar: symbolTable.getLocalVariables(tempMethod)) {
            if(localVar.getName().equals(childName))
                return true;
        }

        for (var param: symbolTable.getParameters(tempMethod)) {
            if(param.getName().equals(childName))
                return true;
        }

        for (var field : symbolTable.getFields()){
            if(field.getName().equals(childName))
                return true;
        }

        for (var imports : symbolTable.getImports()){
            if(childName.equals(imports))
                return true;
        }
        return false;
    }

    @Override
    protected void buildVisitor() {

    }
}
