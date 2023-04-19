package pt.up.fe.comp2023.Analysers;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.report.StageResult;
import pt.up.fe.comp2023.SymbolTable;
import pt.up.fe.comp2023.visitors.SymbolTableVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignType extends SymbolTableVisitor implements StageResult{
    private SymbolTable symbolTable;
    private List<Report> reports;

    public AssignType(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        buildVisitor();
        visit(rootNode);
    }

    public Integer assignVisit(JmmNode node, Integer dummy) {
        JmmNode leftChild = node.getJmmChild(0);
        JmmNode rightChild = node.getJmmChild(1);

        //try{
        //        //    leftIdType = getIdType(leftChild).getName();
        //        //}catch(Exception e){
        //        //    if (leftChild.getKind().equals(AstNode.ARRAY_ACCESS_EXPRESSION.toString())){
        //        //        leftIdType = getIdType(leftChild.getJmmChild(0)).getName();
        //        //    }
        //        //}
        String leftIdType = typeCheck(leftChild);

        String rightIdType = typeCheck(rightChild);
        //System.out.println("RIGHT = " + rightIdType);

        if (leftIdType.equals("import")) {
            return 0;
        }
        if (rightIdType.equals("import")) {
            return 0;
        }

        if (rightIdType.equals("new")){

            JmmNode grandchild = rightChild.getJmmChild(0);
            //System.out.println("---> "+ grandchild);

            //só pode ser ArrayDeclaration ou Id

            if(grandchild.getKind().equals("ArrayInstantiation")){
                rightIdType = grandchild.getJmmChild(0).get("type");

            }else{ // ID
                //System.out.println("-------->" + grandchild.get("name"));
                //System.out.println("tou aqui dentro");
                //rightIdType = leftIdType;
                //TODO: caso rightChild = 'new qqcoisa()'
                var c = symbolTable.getClassName();
                if (grandchild.get("name").equals(c)) {
                    rightIdType = c;
                } else {
                    rightIdType = "import";
                }

            }


        }

        if(!leftIdType.equals(rightIdType)){
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                    "Assignment with wrong types"));
        }

        return 0;
    }

    public Type getIdType(JmmNode node){
        JmmNode parentNode = node;
        while (parentNode != null && !parentNode.getKind().equals("methodDeclaration")) {
            parentNode = parentNode.getJmmParent();
        }

        if (parentNode == null) {
            return null;
        }

        String parentMethodName = parentNode.get("name");

        // Local Variables
        for (var localVariable : symbolTable.getLocalVariables(parentMethodName)) {
            if (node.get("name").equals(localVariable.getName())) {
                return localVariable.getType();
            }
        }

        // Parameters
        for (var param : symbolTable.getParameters(parentMethodName)) {
            if (node.get("name").equals(param.getName())) {
                return param.getType();
            }
        }

        // Fields
        for (var field : symbolTable.getFields()) {
            if (node.get("name").equals(field.getName())) {
                return field.getType();
            }
        }
        return null;
    }

    public boolean isTypeExternal(Type type) {
        if (type == null) return false;
        var type_name = type.getName();
        // check extend
        var extend = symbolTable.getSuper();
        // check imports
        var imports = symbolTable.getImports();
        // add extend to loop if not null
//        if (extend != null)           // TODO: MR.GOLOSO PLZ FIX THIS, THIS IS CAUSING TROUBLE IN OLLIR
//            imports.add(0, extend);
        for (var t: imports) {
            if (type_name.equals(t))
                return true;
        }

        return false;
    }

    private String typeCheck(JmmNode node) {
        var myKind = node.getKind();

        if (myKind.equals("ArithmeticBinaryOp") || myKind.equals("BoolBinaryOp")) {
            if (node.get("op").equals("&&") || node.get("op").equals("<")) {
                return "boolean";
            }
            return "int";
        }

        if (myKind.equals("Integer") || myKind.equals("Boolean")) {
            return node.get("type");
        }
        if (myKind.equals("Instantiation")) {
            return "new";
        }
        if (myKind.equals("Identifier")) {
            var type = getIdType(node);
            if (type == null) return "null";
            if (isTypeExternal(type)) {
                return "import";
            }

            return type.getName();
        }
        if (myKind.equals("ArrayAccess")) {
            // if it's an array[] it produces an id as child 0, just return its type
            return typeCheck(node.getJmmChild(0));
        }
        if (myKind.equals("MethodDeclaration")) {
            return "null"; // TODO: implement method
        }

        return "null";
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
