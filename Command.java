public class Command {

    String operator; // "=", "+", "float", "jump", etc.
    String[] operands; // everything after the operator

    public Command(String operator, String[] operands) {
        this.operator = operator;
        this.operands = operands;
    }

    public String getOperator() {
        return operator;
    }

    public String[] getOperands() {
        return operands;
    }
}
