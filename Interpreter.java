import java.io.*;
import java.util.*;

public class Interpreter {

    public static void main(String[] args) {
        if (!(args.length > 0)) {
            System.out.println("Usage: java Interpreter <bytecode_file>");
            return;
        }

        // Initialize variables
        HashMap<String, Register> variables = new HashMap<>();
        HashMap<String, Integer> labelMap = new HashMap<>();
        ArrayList<Command> commands = new ArrayList<>();
        ArrayList<String> tokens = new ArrayList<>();

        // Read in file and print it out
        File bytecodeFile = new File(args[0]);
        try (Scanner myReader = new Scanner(bytecodeFile)) {
            while (myReader.hasNext()) {
                String data = myReader.next();
                tokens.add(data);
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        commands = parseCommands(tokens);

        // Loop through command list and set the labels
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).getOperator().equals("label")) {
                labelMap.put(commands.get(i).getOperands()[0], i);
            }
        }

        // Execute commands
        int pc = 0;

        while (pc < commands.size()) {
            Command cmd = commands.get(pc);
            switch (cmd.getOperator()) {
                case "function":
                    // Store any parameters in the variables hashmap
                    if(cmd.getOperands().length > 2) {
                        for(int i = 2; i < cmd.getOperands().length - 1; i+=2) {
                            variables.put(cmd.getOperands()[i+1], new Register(cmd.getOperands()[i]));
                        }
                    }
                    break;
                case "endfunction":
                    // Doesn't do anything since this marks the end of the function.
                    break;
                case "callfunction":
                    // Check if it's a print function
                    if (cmd.getOperands()[0].equals("print")) {
                        String label = cmd.getOperands()[2];
                        if (variables.containsKey(label)) {
                            label = variables.get(label).getValue().toString();
                        } else {
                            // Strip quotes
                            label = label.replace("\"", "");
                        }
                        // Resolve value
                        double printVal = resolveFloat(cmd.getOperands()[3], variables);
                        System.out.println(label + ": " + printVal);
                    }
                    break;
                case "return":
                    // Set the program counter to the end to exit the loop
                    pc = commands.size();
                    break;
                case "jump":
                    pc = labelMap.get(cmd.getOperands()[0]);
                    continue; // Continue to skip the pc increment at the bottom of the loop
                case "jumpif":
                    double boolVal = resolveFloat(cmd.getOperands()[1], variables);
                    if (boolVal != 0.0) {
                        pc = labelMap.get(cmd.getOperands()[0]);
                        continue; // Continue to skip the pc increment at the bottom of the loop
                    }
                    break;
                case "label":
                    // Nothing to do here since we created all the labels already
                    break;
                case "float":
                    variables.put(cmd.getOperands()[0], new Register("float"));
                    break;
                case "vector3":
                    // Adds the main vector variable along with its children
                    variables.put(
                        cmd.getOperands()[0],
                        new Register("vector3")
                    );
                    variables.put(
                        cmd.getOperands()[0] + ".r",
                        new Register("float")
                    );
                    variables.put(
                        cmd.getOperands()[0] + ".g",
                        new Register("float")
                    );
                    variables.put(
                        cmd.getOperands()[0] + ".b",
                        new Register("float")
                    );
                    break;
                case "bool":
                    variables.put(cmd.getOperands()[0], new Register("bool"));
                    break;
                case "string":
                    variables.put(cmd.getOperands()[0], new Register("string"));
                    break;
                case "=":
                    Register destinationReg = variables.get(cmd.getOperands()[0]);
                    String rhsValue = cmd.getOperands()[1];
                    String destinationType = destinationReg.getType();

                    // We need to check what types we are trying to assign
                    if (destinationType.equals("float") || destinationType.equals("bool")) {
                        destinationReg.setValue(resolveFloat(rhsValue, variables));
                    } else if (destinationType.equals("string")) {
                        // Could be a string register or just a plain string
                        if (variables.containsKey(rhsValue)) {
                            destinationReg.setValue(variables.get(rhsValue).getValue());
                        } else {
                            // Strip quotes
                            destinationReg.setValue(rhsValue.replace("\"", ""));
                        }
                    } else if (destinationType.equals("vector3")) {
                        // For vector3, copy over all the components
                        double rVal = (double) variables.get(rhsValue + ".r").getValue();
                        double gVal = (double) variables.get(rhsValue + ".g").getValue();
                        double bVal = (double) variables.get(rhsValue + ".b").getValue();
                        variables.get(cmd.getOperands()[0] + ".r").setValue(rVal);
                        variables.get(cmd.getOperands()[0] + ".g").setValue(gVal);
                        variables.get(cmd.getOperands()[0] + ".b").setValue(bVal);
                    }
                    break;
                case "==": {
                    // Store the left and right operands
                    String left = cmd.getOperands()[1];
                    String right = cmd.getOperands()[2];
                    boolean result;

                    // Check type by checking if operands are vector3 registers
                    if (variables.containsKey(left) && variables.get(left).getType().equals("vector3")) {
                        // vector3 check. Compares rgb
                        double lr = (double) variables
                            .get(left + ".r")
                            .getValue();
                        double lg = (double) variables
                            .get(left + ".g")
                            .getValue();
                        double lb = (double) variables
                            .get(left + ".b")
                            .getValue();
                        double rr = (double) variables
                            .get(right + ".r")
                            .getValue();
                        double rg = (double) variables
                            .get(right + ".g")
                            .getValue();
                        double rb = (double) variables
                            .get(right + ".b")
                            .getValue();
                        result = (lr == rr && lg == rg && lb == rb);
                    } else {
                        // Normal bool and float comparison
                        double l = resolveFloat(left, variables);
                        double r = resolveFloat(right, variables);
                        result = (l == r);
                    }
                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(result ? 1.0 : 0.0);
                    break;
                }
                case "!=":
                    // Store the left and right operands
                    String left = cmd.getOperands()[1];
                    String right = cmd.getOperands()[2];
                    boolean result;

                    // Determine type by checking if operands are vector3 registers
                    if (
                        variables.containsKey(left) &&
                        variables.get(left).getType().equals("vector3")
                    ) {
                        // vector3 check. Compares rgb
                        double lr = (double) variables
                            .get(left + ".r")
                            .getValue();
                        double lg = (double) variables
                            .get(left + ".g")
                            .getValue();
                        double lb = (double) variables
                            .get(left + ".b")
                            .getValue();
                        double rr = (double) variables
                            .get(right + ".r")
                            .getValue();
                        double rg = (double) variables
                            .get(right + ".g")
                            .getValue();
                        double rb = (double) variables
                            .get(right + ".b")
                            .getValue();
                        result = (lr == rr && lg == rg && lb == rb);
                    } else {
                        // Normal bool and float comparison
                        double l = resolveFloat(left, variables);
                        double r = resolveFloat(right, variables);
                        result = (l == r);
                    }
                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(result ? 0.0 : 1.0); // Same logic as ==, just flip the 0.0 and 1.0.
                    break;
                case "<=":
                    // Store the left and right operands
                    double lteLeftOperand = resolveFloat(
                        cmd.getOperands()[1],
                        variables
                    );
                    double lteRightOperand = resolveFloat(
                        cmd.getOperands()[2],
                        variables
                    );
                    boolean lessThanOrEqualsToo;

                    lessThanOrEqualsToo = (lteLeftOperand <= lteRightOperand);

                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(lessThanOrEqualsToo ? 1.0 : 0.0);
                    break;
                case ">=":
                    // Store the left and right operands
                    double gteLeftOperand = resolveFloat(
                        cmd.getOperands()[1],
                        variables
                    );
                    double gteRightOperand = resolveFloat(
                        cmd.getOperands()[2],
                        variables
                    );
                    boolean greaterThanOrEqualsToo;

                    greaterThanOrEqualsToo = (gteLeftOperand >= gteRightOperand);

                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(greaterThanOrEqualsToo ? 1.0 : 0.0);
                    break;
                case "<":
                    // Store the left and right operands
                    double ltLeftOperand = resolveFloat(
                        cmd.getOperands()[1],
                        variables
                    );
                    double ltRightOperand = resolveFloat(
                        cmd.getOperands()[2],
                        variables
                    );
                    boolean lessThan;

                    lessThan = (ltLeftOperand < ltRightOperand);

                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(lessThan ? 1.0 : 0.0);
                    break;
                case ">":
                    // Store the left and right operands
                    double gtLeftOperand = resolveFloat(
                        cmd.getOperands()[1],
                        variables
                    );
                    double gtRightOperand = resolveFloat(
                        cmd.getOperands()[2],
                        variables
                    );
                    boolean greaterThan;

                    greaterThan = (gtLeftOperand > gtRightOperand);

                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(greaterThan ? 1.0 : 0.0);
                    break;
                case "+":
                    double addLeft = resolveFloat(
                        cmd.getOperands()[1],
                        variables
                    );
                    double addRight = resolveFloat(
                        cmd.getOperands()[2],
                        variables
                    );

                    // Sets the value in the hashmap
                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(addLeft + addRight);
                    break;
                case "-":
                    double subLeft = resolveFloat(
                        cmd.getOperands()[1],
                        variables
                    );
                    double subRight = resolveFloat(
                        cmd.getOperands()[2],
                        variables
                    );
                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(subLeft - subRight);
                    break;
                case "*":
                    double mulLeft = resolveFloat(
                        cmd.getOperands()[1],
                        variables
                    );
                    double mulRight = resolveFloat(
                        cmd.getOperands()[2],
                        variables
                    );
                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(mulLeft * mulRight);
                    break;
                case "/":
                    double divLeft = resolveFloat(
                        cmd.getOperands()[1],
                        variables
                    );
                    double divRight = resolveFloat(
                        cmd.getOperands()[2],
                        variables
                    );
                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(divLeft / divRight);
                    break;
                case "%":
                    double modLeft = resolveFloat(
                        cmd.getOperands()[1],
                        variables
                    );
                    double modRight = resolveFloat(
                        cmd.getOperands()[2],
                        variables
                    );
                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(modLeft % modRight);
                    break;
                case "&":
                    double andLeft = resolveFloat(
                        cmd.getOperands()[1],
                        variables
                    );
                    double andRight = resolveFloat(
                        cmd.getOperands()[2],
                        variables
                    );
                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(
                            (andLeft != 0.0 && andRight != 0.0) ? 1.0 : 0.0
                        );
                    break;
                case "|":
                    double orLeft = resolveFloat(
                        cmd.getOperands()[1],
                        variables
                    );
                    double orRight = resolveFloat(
                        cmd.getOperands()[2],
                        variables
                    );
                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(
                            (orLeft != 0.0 || orRight != 0.0) ? 1.0 : 0.0
                        );
                    break;
                case "!":
                    double notVal = resolveFloat(
                        cmd.getOperands()[1],
                        variables
                    );

                    variables
                        .get(cmd.getOperands()[0])
                        .setValue(notVal == 0.0 ? 1.0 : 0.0);
                    break;
                default:
                    System.out.println("Unknown command: " + cmd.getOperator());
                    break;
            }
            pc++;
        }
    }

    public static ArrayList<Command> parseCommands(ArrayList<String> tokens) {
        ArrayList<Command> listToReturn = new ArrayList<>();
        int i = 0;

        while (i < tokens.size()) {
            String current = tokens.get(i);

            if (current.equals("function")) {
                // function <name> params <arg1> … <argn> endparams
                ArrayList<String> params = new ArrayList<>();
                i++; // Skip "function"
                while (
                    i < tokens.size() && !tokens.get(i).equals("endparams")
                ) {
                    params.add(tokens.get(i));
                    i++;
                }
                i++; // Skip over endparams
                listToReturn.add(
                    new Command("function", params.toArray(new String[0]))
                );
            } else if (current.equals("endfunction")) {
                listToReturn.add(new Command("endfunction", new String[0]));
                i++;
            } else if (current.equals("callfunction")) {
                // callfunction <name> <variable_to_hold_return> args <args1>  … <argsn> endargs
                ArrayList<String> params = new ArrayList<>();
                i++;
                while (i < tokens.size() && !tokens.get(i).equals("endargs")) {
                    params.add(tokens.get(i));
                    i++;
                }
                i++; // Skip over endargs
                listToReturn.add(
                    new Command("callfunction", params.toArray(new String[0]))
                );
            } else if (current.equals("return")) {
                // return <var> endreturn OR return endreturn

                ArrayList<String> params = new ArrayList<>();
                i++;
                while (
                    i < tokens.size() && !tokens.get(i).equals("endreturn")
                ) {
                    params.add(tokens.get(i));
                    i++;
                }
                i++; // Skip over endreturn
                listToReturn.add(
                    new Command("return", params.toArray(new String[0]))
                );
            } else if (current.equals("jump")) {
                // jump <label>
                i++;
                listToReturn.add(
                    new Command("jump", new String[] { tokens.get(i) })
                );
                i++;
            } else if (current.equals("jumpif")) {
                // jump <label> <bool>
                listToReturn.add(
                    new Command(
                        "jumpif",
                        new String[] { tokens.get(i + 1), tokens.get(i + 2) }
                    )
                );
                // Skip ahead 3 spaces since we're reading in jump, <label>, and <bool>
                i += 3;
            } else if (current.equals("label")) {
                // label <name>
                listToReturn.add(
                    new Command("label", new String[] { tokens.get(i + 1) })
                );
                i += 2;
            } else if (current.equals("float")) {
                listToReturn.add(
                    new Command("float", new String[] { tokens.get(i + 1) })
                );
                i += 2;
            } else if (current.equals("vector3")) {
                listToReturn.add(
                    new Command("vector3", new String[] { tokens.get(i + 1) })
                );
                i += 2;
            } else if (current.equals("bool")) {
                listToReturn.add(
                    new Command("bool", new String[] { tokens.get(i + 1) })
                );
                i += 2;
            } else if (current.equals("string")) {
                listToReturn.add(
                    new Command("string", new String[] { tokens.get(i + 1) })
                );
                i += 2;
            } else if (current.equals("=")) {
                // = <reg> <reg or const>
                listToReturn.add(
                    new Command(
                        "=",
                        new String[] { tokens.get(i + 1), tokens.get(i + 2) }
                    )
                );
                i += 3;
            } else if (current.equals("==")) {
                // == <reg> <reg or const> <reg or const>
                listToReturn.add(
                    new Command(
                        "==",
                        new String[] {
                            tokens.get(i + 1),
                            tokens.get(i + 2),
                            tokens.get(i + 3),
                        }
                    )
                );
                i += 4;
            } else if (current.equals("!=")) {
                // != <reg> <reg or const> <reg or const>
                listToReturn.add(
                    new Command(
                        "!=",
                        new String[] {
                            tokens.get(i + 1),
                            tokens.get(i + 2),
                            tokens.get(i + 3),
                        }
                    )
                );
                i += 4;
            } else if (current.equals("<=")) {
                // <= <reg> <reg or const> <reg or const>
                listToReturn.add(
                    new Command(
                        "<=",
                        new String[] {
                            tokens.get(i + 1),
                            tokens.get(i + 2),
                            tokens.get(i + 3),
                        }
                    )
                );
                i += 4;
            } else if (current.equals(">=")) {
                // >= <reg> <reg or const> <reg or const>
                listToReturn.add(
                    new Command(
                        ">=",
                        new String[] {
                            tokens.get(i + 1),
                            tokens.get(i + 2),
                            tokens.get(i + 3),
                        }
                    )
                );
                i += 4;
            } else if (current.equals("<")) {
                // < <reg> <reg or const> <reg or const>
                listToReturn.add(
                    new Command(
                        "<",
                        new String[] {
                            tokens.get(i + 1),
                            tokens.get(i + 2),
                            tokens.get(i + 3),
                        }
                    )
                );
                i += 4;
            } else if (current.equals(">")) {
                // > <reg> <reg or const> <reg or const>
                listToReturn.add(
                    new Command(
                        ">",
                        new String[] {
                            tokens.get(i + 1),
                            tokens.get(i + 2),
                            tokens.get(i + 3),
                        }
                    )
                );
                i += 4;
            } else if (current.equals("+")) {
                // + <reg> <reg or const> <reg or const>
                listToReturn.add(
                    new Command(
                        "+",
                        new String[] {
                            tokens.get(i + 1),
                            tokens.get(i + 2),
                            tokens.get(i + 3),
                        }
                    )
                );
                i += 4;
            } else if (current.equals("-")) {
                // - <reg> <reg or const> <reg or const>
                listToReturn.add(
                    new Command(
                        "-",
                        new String[] {
                            tokens.get(i + 1),
                            tokens.get(i + 2),
                            tokens.get(i + 3),
                        }
                    )
                );
                i += 4;
            } else if (current.equals("*")) {
                // * <reg> <reg or const> <reg or const>
                listToReturn.add(
                    new Command(
                        "*",
                        new String[] {
                            tokens.get(i + 1),
                            tokens.get(i + 2),
                            tokens.get(i + 3),
                        }
                    )
                );
                i += 4;
            } else if (current.equals("/")) {
                // / <reg> <reg or const> <reg or const>
                listToReturn.add(
                    new Command(
                        "/",
                        new String[] {
                            tokens.get(i + 1),
                            tokens.get(i + 2),
                            tokens.get(i + 3),
                        }
                    )
                );
                i += 4;
            } else if (current.equals("%")) {
                // % <reg> <reg or const> <reg or const>
                listToReturn.add(
                    new Command(
                        "%",
                        new String[] {
                            tokens.get(i + 1),
                            tokens.get(i + 2),
                            tokens.get(i + 3),
                        }
                    )
                );
                i += 4;
            } else if (current.equals("&")) {
                // & <reg> <reg or const> <reg or const>
                listToReturn.add(
                    new Command(
                        "&",
                        new String[] {
                            tokens.get(i + 1),
                            tokens.get(i + 2),
                            tokens.get(i + 3),
                        }
                    )
                );
                i += 4;
            } else if (current.equals("|")) {
                // | <reg> <reg or const> <reg or const>
                listToReturn.add(
                    new Command(
                        "|",
                        new String[] {
                            tokens.get(i + 1),
                            tokens.get(i + 2),
                            tokens.get(i + 3),
                        }
                    )
                );
                i += 4;
            } else if (current.equals("!")) {
                // ! <reg> <reg or const>
                listToReturn.add(
                    new Command(
                        "!",
                        new String[] { tokens.get(i + 1), tokens.get(i + 2) }
                    )
                );
                i += 3;
            } else {
                // Unknown token so print error.
                System.err.println("Unknown token: " + current);
                i++;
            }
        }
        return listToReturn;
    }

    // Parses a string into a float. If the value is stored in a register, we parse that too.
    public static double resolveFloat(
        String operand,
        HashMap<String, Register> registers
    ) {
        if (registers.containsKey(operand)) {
            return Double.valueOf(registers.get(operand).getValue().toString());
        } else {
            return Double.parseDouble(operand);
        }
    }
}
