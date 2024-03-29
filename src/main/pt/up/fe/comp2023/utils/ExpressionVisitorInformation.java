package pt.up.fe.comp2023.utils;

import java.util.ArrayList;
import java.util.List;

public class ExpressionVisitorInformation {

    private Boolean shouldAppendType;
    private Integer auxVariables;
    private List<String> auxLines;

    private String resultName;
    private String ollirType;

    public ExpressionVisitorInformation() {
        this.auxVariables = 0;
        this.auxLines = new ArrayList<>();
        this.resultName = "";
        this.ollirType = "";
        this.shouldAppendType = true;
    }

    public ExpressionVisitorInformation(Boolean shouldAppendType) {
        this.auxVariables = 0;
        this.auxLines = new ArrayList<>();
        this.resultName = "";
        this.ollirType = "";
        this.shouldAppendType = shouldAppendType;
    }


    public Integer getAuxVariables() {
        return auxVariables;
    }

    public void setAuxVariables(Integer auxVariables) {
        this.auxVariables = auxVariables;
    }

    public List<String> getAuxLines() {
        return auxLines;
    }

    public void setAuxLines(List<String> auxLines) {
        this.auxLines = auxLines;
    }

    public void addAuxLine(String auxLine) {
        this.auxLines.add(auxLine);
        this.auxVariables += 1;
    }

    public void addAuxLines(List<String> auxLines) {
        this.auxLines.addAll(auxLines);
    }

    public String getResultNameAndType() {
        if (this.shouldAppendType)
            return resultName + "." + ollirType;
        else
            return resultName;
    }

    public String getOllirType() {
        return ollirType;
    }

    public void setOllirType(String ollirType) {
        this.ollirType = ollirType;
    }

    public String getResultName() {
        return resultName;
    }

    public void setResultName(String resultName) {
        this.resultName = resultName;
    }

    public Boolean getShouldAppendType() {
        return shouldAppendType;
    }

    public void setShouldAppendType(Boolean shouldAppendType) {
        this.shouldAppendType = shouldAppendType;
    }
}
