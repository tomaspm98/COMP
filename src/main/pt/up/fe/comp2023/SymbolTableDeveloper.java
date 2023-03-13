package pt.up.fe.comp2023;

import pt.up.fe.comp2023.node.information.Method;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.collections.*;

import java.util.*;

public class SymbolTableDeveloper implements SymbolTable {
    final private SpecsList<Method> methods;
    final private List<String> imports;
    private List<Symbol> fields;
    private String className;
    private String superClassName;
    private Method currentMethod;

    public SymbolTableDeveloper() {
        this.methods = SpecsList.newInstance(Method.class);
        this.imports = new ArrayList<>();
        this.className = "";
        this.superClassName = "";
    }

    @Override
    public List<String> getImports() {
        return this.imports;
    }

    public void addImport(String s) {
        imports.add(s);
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String getSuper() {
        return this.superClassName.isEmpty() ? null : this.superClassName;
    }

    public void setSuper(String superClassName) {
        this.superClassName = superClassName;
    }

    @Override
    public List<Symbol> getFields() {
        return this.fields.isEmpty() ? null : this.fields;
    }

    public void setFields(List<Symbol> fields) {
        this.fields = fields;
    }

    @Override
    public List<String> getMethods() {
        List<String> methods = this.methods.stream().map(Method::getName).toList();
        return (methods.isEmpty() ? null : methods);
    }

    @Override
    public Type getReturnType(String s) {
        for (Method method : this.methods) {
            if (method.getName().equals(s)) {
                return method.getRetType();
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getParameters(String s) {
        for (Method method : this.methods) {
            if (method.getName().equals(s)) {
                return method.getParameters();
            }
        }
        return null;
    }

    public void addMethod(String name, String returnType) {
        Method currentMethod = new Method(name, returnType);
        this.methods.add(currentMethod);
    }

    public void addMethod(String methodName, Type type) {
        this.methods.add(new Method(methodName, type));
    }

    public void addMethod(Method method) {
        this.methods.add(method);
    }
    @Override
    public List<Symbol> getLocalVariables(String s) {
        for (Method method : methods) {
            if (method.getName().equals(s)) {
                return method.getVariables();
            }
        }
        return null;
    }

    public Optional<Method> getMethodTry(String s) {
        for (Method method : this.methods) {
            if (method.getName().equals(s)) return Optional.of(method);
        }
        return Optional.empty();
    }

}
