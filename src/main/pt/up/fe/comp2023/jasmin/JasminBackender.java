package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

import static org.specs.comp.ollir.InstructionType.BINARYOPER;
import static org.specs.comp.ollir.InstructionType.RETURN;


public class JasminBackender implements JasminBackend {
    ClassUnit classUnit = null;
    int conditionalNumber = 0;
    int methodStackLimit = 0;
    int currentStack = 0;
    String superClass;

    //it generates the jasmin code
    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        try {
            this.classUnit = ollirResult.getOllirClass();

            this.classUnit.checkMethodLabels();
            this.classUnit.buildCFGs();
            this.classUnit.buildVarTables();


            System.out.println("Jasmin code generation ...");


            String jasmin = buildJasmin();


            List<Report> reports = new ArrayList<>();


            if (ollirResult.getConfig().get("debug") != null && ollirResult.getConfig().get("debug").equals("true")) {
                System.out.println("JASMIN CODE : \n" + jasmin);
            }


            return new JasminResult(ollirResult, jasmin, reports);

        } catch (OllirErrorException e) {
            return new JasminResult(classUnit.getClassName(), null,
                    Collections.singletonList(Report.newError(Stage.GENERATION, -1, -1,
                            "Jasmin generation exception.", e)));
        }

    }


    private String buildJasmin() {

        StringBuilder stringBuilder = new StringBuilder();


        stringBuilder.append(".class ").append(this.classUnit.getClassName()).append("\n");


        this.superClass = this.classUnit.getSuperClass();


        if (this.superClass == null) {
            this.superClass = "java/lang/Object";
        }


        stringBuilder.append(".super ").append(getClassName(this.superClass)).append("\n");

        for (Field field : this.classUnit.getFields()) {
            StringBuilder accessSpec = new StringBuilder();
            if (field.getFieldAccessModifier() != AccessModifiers.DEFAULT) {
                accessSpec.append(field.getFieldAccessModifier().name().toLowerCase()).append(" ");
            }


            if (field.isStaticField()) {
                accessSpec.append("static ");
            }

            if (field.isInitialized()) {
                accessSpec.append("final ");
            }


            stringBuilder.append(".field ").append(accessSpec).append(field.getFieldName())
                    .append(" ").append(this.getFieldDescriptor(field.getFieldType())).append("\n");
        }


        for (Method method : this.classUnit.getMethods()) {

            stringBuilder.append(this.getHeaderMethod(method));
            stringBuilder.append(this.getStatementsMethod(method));
            stringBuilder.append(".end method\n");

        }

        return stringBuilder.toString();
    }


    //obtains the method header
    private String getHeaderMethod(Method method) {
        StringBuilder stringBuilder = new StringBuilder("\n.method ");

        if (method.getMethodAccessModifier() != AccessModifiers.DEFAULT) {
            stringBuilder.append(method.getMethodAccessModifier().name().toLowerCase()).append(" ");
        }

        if (method.isStaticMethod()) stringBuilder.append("static ");
        if (method.isFinalMethod()) stringBuilder.append("final ");

        if (method.isConstructMethod()) stringBuilder.append("<init>");
        else stringBuilder.append(method.getMethodName());
        stringBuilder.append("(");

        for (Element param : method.getParams()) {
            stringBuilder.append(this.getFieldDescriptor(param.getType()));
        }
        stringBuilder.append(")");
        stringBuilder.append(this.getFieldDescriptor(method.getReturnType())).append("\n");

        return stringBuilder.toString();
    }


    //obtains all the statements present on the method
    private String getStatementsMethod(Method method) {

        int limitLocals = calculateLimitLocals(method);

        this.currentStack = 0;
        this.methodStackLimit = 0;

        String methodInstructions = this.getInstructionsMethod(method);


        return "\t.limit stack " + this.methodStackLimit + "\n" +
                "\t.limit locals " + limitLocals + "\n" +
                methodInstructions;
    }


    //gets all the instructions on the method
    private String getInstructionsMethod(Method method) {
        StringBuilder stringBuilder = new StringBuilder();

        List<Instruction> instructionsMethod = method.getInstructions();

        for (Instruction instruction : instructionsMethod) {
            for (Map.Entry<String, Instruction> label : method.getLabels().entrySet()) {
                if (label.getValue().equals(instruction)) {
                    stringBuilder.append(label.getKey()).append(":\n");
                }
            }

            stringBuilder.append(this.getInstruction(instruction, method.getVarTable()));
            if (instruction.getInstType() == InstructionType.CALL
                    && ((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID) {

                stringBuilder.append("\tpop\n");
                this.changeStackLimits(-1);
            }

        }

        boolean hasReturnInstruction = instructionsMethod.size() > 0
                && instructionsMethod.get(instructionsMethod.size() - 1).getInstType() == RETURN;

        if (!hasReturnInstruction && method.getReturnType().getTypeOfElement() == ElementType.VOID) {
            stringBuilder.append("\treturn\n");
        }

        return stringBuilder.toString();
    }


    //gets the instructions of all types
    private String getInstruction(Instruction instruction, HashMap<String, Descriptor> varTable) {
        return switch (instruction.getInstType()) {
            case GOTO -> this.getGotoInstruction((GotoInstruction) instruction);
            case ASSIGN -> this.getAssignInstruction((AssignInstruction) instruction, varTable);
            case CALL -> this.getCallInstruction((CallInstruction) instruction, varTable);
            case BRANCH -> this.getBranchInstruction((CondBranchInstruction) instruction, varTable);
            case GETFIELD -> this.getGetFieldInstruction((GetFieldInstruction) instruction, varTable);
            case RETURN -> this.getReturnInstruction((ReturnInstruction) instruction, varTable);
            case PUTFIELD -> this.getPutFieldInstruction((PutFieldInstruction) instruction, varTable);
            case NOPER -> this.getLoadToStack(((SingleOpInstruction) instruction).getSingleOperand(), varTable);
            case BINARYOPER -> this.getBinaryOpInstruction((BinaryOpInstruction) instruction, varTable);
            case UNARYOPER -> this.getUnaryOpInstruction((UnaryOpInstruction) instruction, varTable);
        };
    }


    //gets only UNARY operations instructions
    private String getUnaryOpInstruction(UnaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.getLoadToStack(instruction.getOperand(), varTable))
                .append("\t").append(this.getOp(instruction.getOperation()));

        boolean isBoolOp = instruction.getOperation().getOpType() == OperationType.NOTB;
        if (isBoolOp) {
            stringBuilder.append(this.getBooleanOpResultToStack());
        } else {
            stringBuilder.append("; Invalid UNARYOPERATOR\n");
        }

        stringBuilder.append("\n");
        return stringBuilder.toString();
    }


    //gets only BINARY operations instructions
    private String getBinaryOpInstruction(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        Element rightElem = instruction.getRightOperand();
        Element leftElem = instruction.getLeftOperand();

        stringBuilder.append(this.getLoadToStack(leftElem, varTable))
                .append(this.getLoadToStack(rightElem, varTable))
                .append("\t").append(this.getOp(instruction.getOperation()));

        OperationType typeOp = instruction.getOperation().getOpType();
        boolean isBooleanOperation =
                typeOp == OperationType.EQ
                        || typeOp == OperationType.GTH
                        || typeOp == OperationType.GTE
                        || typeOp == OperationType.LTH
                        || typeOp == OperationType.LTE
                        || typeOp == OperationType.NEQ;

        if (isBooleanOperation) {
            stringBuilder.append(this.getBooleanOpResultToStack());
        }

        stringBuilder.append("\n");

        this.changeStackLimits(-1);
        return stringBuilder.toString();
    }


    //gets only BRANCH instructions
    private String getBranchInstruction(CondBranchInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        Instruction condition;
        if (instruction instanceof SingleOpCondInstruction) {
            SingleOpCondInstruction singleOpCondInstruction = (SingleOpCondInstruction) instruction;
            condition = singleOpCondInstruction.getCondition();

        } else if (instruction instanceof OpCondInstruction) {
            OpCondInstruction opCondInstruction = (OpCondInstruction) instruction;
            condition = opCondInstruction.getCondition();

        } else {
            return "; ERROR: invalid CondBranchInstruction instance\n";
        }

        String operation;

        switch (condition.getInstType()) {
            case BINARYOPER -> {
                BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) condition;
                switch (binaryOpInstruction.getOperation().getOpType()) {
                    case LTH -> {
                        Element leftElement = binaryOpInstruction.getLeftOperand();
                        Element rightElement = binaryOpInstruction.getRightOperand();

                        Integer parsedInt = null;
                        Element otherElement = null;
                        operation = "if_icmplt";

                        if (leftElement instanceof LiteralElement) {
                            String literal = ((LiteralElement) leftElement).getLiteral();
                            parsedInt = Integer.parseInt(literal);
                            otherElement = rightElement;
                            operation = "ifgt";

                        } else if (rightElement instanceof LiteralElement) {
                            String literal = ((LiteralElement) rightElement).getLiteral();
                            parsedInt = Integer.parseInt(literal);
                            otherElement = leftElement;
                            operation = "iflt";
                        }

                        if (parsedInt != null && parsedInt == 0) {
                            stringBuilder.append(this.getLoadToStack(otherElement, varTable));

                        } else {
                            stringBuilder.append(this.getLoadToStack(leftElement, varTable))
                                    .append(this.getLoadToStack(rightElement, varTable));

                            operation = "if_icmplt";
                        }

                    }
                    case ANDB -> {
                        stringBuilder.append(this.getInstruction(condition, varTable));
                        operation = "ifne";
                    }
                    default -> {
                        stringBuilder.append("; Invalid BINARYOPER\n");
                        stringBuilder.append(this.getInstruction(condition, varTable));
                        operation = "ifne";
                    }
                }
            }
            case UNARYOPER -> {
                UnaryOpInstruction unaryOpInstruction = (UnaryOpInstruction) condition;
                if (unaryOpInstruction.getOperation().getOpType() == OperationType.NOTB) {
                    stringBuilder.append(this.getLoadToStack(unaryOpInstruction.getOperand(), varTable));
                    operation = "ifeq";
                } else {
                    stringBuilder.append("; Invalid UNARYOPER\n");
                    stringBuilder.append(this.getInstruction(condition, varTable));
                    operation = "ifne";
                }
            }
            default -> {
                stringBuilder.append(this.getInstruction(condition, varTable));
                operation = "ifne";
            }
        }

        stringBuilder.append("\t").append(operation).append(" ").append(instruction.getLabel()).append("\n");

        if (operation.equals("if_icmplt")) {
            this.changeStackLimits(-2);
        } else {
            this.changeStackLimits(-1);
        }

        return stringBuilder.toString();
    }


    //get only the operation
    private String getOp(Operation operation) {
        var op = operation.getOpType();
        return switch (op) {
            case LTH -> "if_icmplt";
            case ANDB -> "iand";
            case NOTB -> "ifeq";

            case ADD -> "iadd";
            case SUB -> "isub";
            case MUL -> "imul";
            case DIV -> "idiv";

            default -> "; ERROR: operation not implemented: " + operation.getOpType() + "\n";
        };
    }


    //gets the PUT FIELD instructions
    private String getPutFieldInstruction(PutFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        String result = this.getLoadToStack(instruction.getFirstOperand(), varTable) +
                this.getLoadToStack(instruction.getThirdOperand(), varTable) +
                "\tputfield " + this.getClassName(((Operand) instruction.getFirstOperand()).getName()) +
                "/" + ((Operand) instruction.getSecondOperand()).getName() +
                " " + this.getFieldDescriptor(instruction.getSecondOperand().getType()) + "\n";


        this.changeStackLimits(-2);
        return result;
    }


    //gets the GET FIELD isntructions
    private String getGetFieldInstruction(GetFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        return this.getLoadToStack(instruction.getFirstOperand(), varTable) +
                "\tgetfield " + this.getClassName(((Operand) instruction.getFirstOperand()).getName()) +
                "/" + ((Operand) instruction.getSecondOperand()).getName() +
                " " + this.getFieldDescriptor(instruction.getSecondOperand().getType()) + "\n";
    }


    //gets the RETURN instruction
    private String getReturnInstruction(ReturnInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        if (instruction.hasReturnValue()) {
            stringBuilder.append(this.getLoadToStack(instruction.getOperand(), varTable));
        }

        stringBuilder.append("\t");
        if (instruction.getOperand() != null) {
            ElementType elementType = instruction.getOperand().getType().getTypeOfElement();

            if (elementType == ElementType.INT32 || elementType == ElementType.BOOLEAN) {
                stringBuilder.append("i");
            } else {
                stringBuilder.append("a");
            }
        }

        stringBuilder.append("return\n");

        return stringBuilder.toString();
    }


    //gets the GO TO instruction
    private String getGotoInstruction(GotoInstruction instruction) {
        return "\tgoto " + instruction.getLabel() + "\n";
    }


    //load variables or literals into the JVM stack
    private String getLoadToStack(Element element, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        if (element instanceof LiteralElement) {
            String literal = ((LiteralElement) element).getLiteral();

            if (element.getType().getTypeOfElement() == ElementType.INT32
                    || element.getType().getTypeOfElement() == ElementType.BOOLEAN) {

                int parsedInt = Integer.parseInt(literal);

                if (parsedInt >= -1 && parsedInt <= 5) { // [-1,5]
                    stringBuilder.append("\ticonst_");
                } else if (parsedInt >= -128 && parsedInt <= 127) { // byte
                    stringBuilder.append("\tbipush ");
                } else if (parsedInt >= -32768 && parsedInt <= 32767) { // short
                    stringBuilder.append("\tsipush ");
                } else {
                    stringBuilder.append("\tldc "); // int
                }

                if (parsedInt == -1) {
                    stringBuilder.append("m1");
                } else {
                    stringBuilder.append(parsedInt);
                }

            } else {
                stringBuilder.append("\tldc ").append(literal);
            }

            this.changeStackLimits(+1);

        } else if (element instanceof ArrayOperand) {
            ArrayOperand operand = (ArrayOperand) element;

            stringBuilder.append("\taload").append(this.getVarNumber(operand.getName(), varTable)).append("\n"); // load array (ref)
            this.changeStackLimits(+1);

            stringBuilder.append(getLoadToStack(operand.getIndexOperands().get(0), varTable)); // load index
            stringBuilder.append("\tiaload"); // load array[index]

            this.changeStackLimits(-1);
        } else if (element instanceof Operand) {
            Operand operand = (Operand) element;
            switch (operand.getType().getTypeOfElement()) {
                case INT32, BOOLEAN -> stringBuilder.append("\tiload").append(this.getVarNumber(operand.getName(), varTable));
                case OBJECTREF, STRING, ARRAYREF -> stringBuilder.append("\taload").append(this.getVarNumber(operand.getName(), varTable));
                case THIS -> stringBuilder.append("\taload_0");
                default -> stringBuilder.append("; ERROR: getLoadToStack() operand ").append(operand.getType().getTypeOfElement()).append("\n");
            }

            this.changeStackLimits(+1);
        } else {
            stringBuilder.append("; ERROR: getLoadToStack() invalid element instance\n");
        }

        stringBuilder.append("\n");
        return stringBuilder.toString();
    }


    //gets only CALL instruction
    private String getCallInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        int numToPop = 0;

        switch (instruction.getInvocationType()) {
            case invokevirtual -> {
                stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));
                numToPop = 1;

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getLoadToStack(element, varTable));
                    numToPop++;
                }

                stringBuilder.append("\tinvokevirtual ")
                        .append(this.getClassName(((ClassType) instruction.getFirstArg().getType()).getName()))
                        .append("/").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""))
                        .append("(");

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getFieldDescriptor(element.getType()));
                }

                stringBuilder.append(")").append(this.getFieldDescriptor(instruction.getReturnType())).append("\n");

                if (instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    numToPop--;
                }

            }
            case invokespecial -> {
                stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));
                numToPop = 1;

                stringBuilder.append("\tinvokespecial ");

                if (instruction.getFirstArg().getType().getTypeOfElement() == ElementType.THIS) {
                    stringBuilder.append(this.superClass);
                } else {
                    String className = this.getClassName(((ClassType) instruction.getFirstArg().getType()).getName());
                    stringBuilder.append(className);
                }

                stringBuilder.append("/").append("<init>(");

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getFieldDescriptor(element.getType()));
                }

                stringBuilder.append(")").append(this.getFieldDescriptor(instruction.getReturnType())).append("\n");

                if (instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    numToPop--;
                }

            }
            case invokestatic -> {
                numToPop = 0;

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getLoadToStack(element, varTable));
                    numToPop++;
                }

                stringBuilder.append("\tinvokestatic ")
                        .append(this.getClassName(((Operand) instruction.getFirstArg()).getName()))
                        .append("/").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""))
                        .append("(");

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getFieldDescriptor(element.getType()));
                }

                stringBuilder.append(")").append(this.getFieldDescriptor(instruction.getReturnType())).append("\n");

                if (instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    numToPop--;
                }

            }
            case NEW -> {
                numToPop = -1;

                ElementType elementType = instruction.getReturnType().getTypeOfElement();

                if (elementType == ElementType.OBJECTREF) {
                    for (Element element : instruction.getListOfOperands()) {
                        stringBuilder.append(this.getLoadToStack(element, varTable));
                        numToPop++;
                    }

                    stringBuilder.append("\tnew ").append(this.getClassName(((Operand) instruction.getFirstArg()).getName())).append("\n");
                } else if (elementType == ElementType.ARRAYREF) {
                    for (Element element : instruction.getListOfOperands()) {
                        stringBuilder.append(this.getLoadToStack(element, varTable));
                        numToPop++;
                    }

                    stringBuilder.append("\tnewarray ");
                    if (instruction.getListOfOperands().get(0).getType().getTypeOfElement() == ElementType.INT32) {
                        stringBuilder.append("int\n");
                    } else {
                        stringBuilder.append("; only int arrays are implemented\n");
                    }

                } else {
                    stringBuilder.append("; ERROR: NEW invocation type not implemented\n");
                }
            }
            case arraylength -> {
                stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));
                stringBuilder.append("\tarraylength\n");
            }
            case ldc -> stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));
            default -> stringBuilder.append("; ERROR: call instruction not implemented\n");
        }


        this.changeStackLimits(-numToPop);


        return stringBuilder.toString();
    }


    //gets only ASSIGN instruction
    private String getAssignInstruction(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        Operand dest = (Operand) instruction.getDest();
        if (dest instanceof ArrayOperand) {
            ArrayOperand arrayOperand = (ArrayOperand) dest;
            this.changeStackLimits(+1);
            stringBuilder.append("\taload").append(this.getVarNumber(arrayOperand.getName(), varTable)).append("\n"); // load array (ref)
            stringBuilder.append(this.getLoadToStack(arrayOperand.getIndexOperands().get(0), varTable)); // load index

        } else {
            if (instruction.getRhs().getInstType() == BINARYOPER) {
                BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) instruction.getRhs();

                if (binaryOpInstruction.getOperation().getOpType() == OperationType.ADD) {
                    boolean leftIsLiteral = binaryOpInstruction.getLeftOperand().isLiteral();
                    boolean rightIsLiteral = binaryOpInstruction.getRightOperand().isLiteral();

                    LiteralElement literal = null;
                    Operand operand = null;

                    if (leftIsLiteral && !rightIsLiteral) {
                        literal = (LiteralElement) binaryOpInstruction.getLeftOperand();
                        operand = (Operand) binaryOpInstruction.getRightOperand();
                    } else if (!leftIsLiteral && rightIsLiteral) {
                        literal = (LiteralElement) binaryOpInstruction.getRightOperand();
                        operand = (Operand) binaryOpInstruction.getLeftOperand();
                    }

                    if (literal != null && operand != null) {
                        if (operand.getName().equals(dest.getName())) {
                            int literalValue = Integer.parseInt((literal).getLiteral());

                            if (literalValue >= -128 && literalValue <= 127) {
                                return "\tiinc " + varTable.get(operand.getName()).getVirtualReg() + " " + literalValue + "\n";
                            }
                        }
                    }

                }
            }
        }

        stringBuilder.append(this.getInstruction(instruction.getRhs(), varTable));
        stringBuilder.append(this.getStore(dest, varTable));

        return stringBuilder.toString();
    }



    private String getStore(Operand dest, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        switch (dest.getType().getTypeOfElement()) {
            case INT32, BOOLEAN -> {
                if (varTable.get(dest.getName()).getVarType().getTypeOfElement() == ElementType.ARRAYREF) {
                    stringBuilder.append("\tiastore").append("\n");
                    this.changeStackLimits(-3);
                } else {
                    stringBuilder.append("\tistore").append(this.getVarNumber(dest.getName(), varTable)).append("\n");
                    this.changeStackLimits(-1);
                }
            }
            case OBJECTREF, THIS, STRING, ARRAYREF -> {
                stringBuilder.append("\tastore").append(this.getVarNumber(dest.getName(), varTable)).append("\n");
                this.changeStackLimits(-1);
            }
            default -> stringBuilder.append("; ERROR: getStore()\n");
        }

        return stringBuilder.toString();
    }


    //gets the number of variables
    private String getVarNumber(String name, HashMap<String, Descriptor> varTable) {
        if (name.equals("this")) {
            return "_0";
        }

        int virtualRegister = varTable.get(name).getVirtualReg();

        StringBuilder stringBuilder = new StringBuilder();

        if (virtualRegister < 4) stringBuilder.append("_");
        else stringBuilder.append(" ");

        stringBuilder.append(virtualRegister);

        return stringBuilder.toString();
    }


    private String getFieldDescriptor(Type type) {
        StringBuilder stringBuilder = new StringBuilder();
        ElementType typeElem = type.getTypeOfElement();

        if (typeElem == ElementType.ARRAYREF) {
            stringBuilder.append("[");
            typeElem = ((ArrayType) type).getArrayType();
        }

        switch (typeElem) {
            case INT32 -> stringBuilder.append("I");
            case BOOLEAN -> stringBuilder.append("Z");
            case OBJECTREF -> {
                String name = ((ClassType) type).getName();
                stringBuilder.append("L").append(this.getClassName(name)).append(";");
            }
            case STRING -> stringBuilder.append("Ljava/lang/String;");
            case VOID -> stringBuilder.append("V");
            default -> stringBuilder.append("; ERROR: descriptor type not implemented\n");
        }

        return stringBuilder.toString();
    }


    //gets the name of the class (full name)
    private String getClassName(String className) {
        if (className.equals("this")) {
            return this.classUnit.getClassName();
        }

        for (String importName : this.classUnit.getImports()) {
            if (importName.endsWith(className)) {
                return importName.replaceAll("\\.", "/");
            }
        }

        return className;
    }


    private String getBooleanOpResultToStack() {
        return " TRUE" + this.conditionalNumber + "\n"
                + "\ticonst_0\n"
                + "\tgoto NEXT" + this.conditionalNumber + "\n"
                + "TRUE" + this.conditionalNumber + ":\n"
                + "\ticonst_1\n"
                + "NEXT" + this.conditionalNumber++ + ":";
    }


    //changes the limits of the stack
    private void changeStackLimits(int variation) {
        this.currentStack += variation;
        this.methodStackLimit = Math.max(this.methodStackLimit, this.currentStack);
    }


    public static int calculateLimitLocals(Method method) {
        Set<Integer> virtualRegisters = new TreeSet<>();
        virtualRegisters.add(0);

        for (Descriptor descriptor : method.getVarTable().values()) {
            virtualRegisters.add(descriptor.getVirtualReg());
        }

        return virtualRegisters.size();
    }

}