import java.io.*;
import java.util.*;

public class Interpreter {

    public static void main(String[] args) {
        if (!(args.length > 0)) {
            System.out.println("Usage: java Interpreter <bytecode_file>");
            return;
        }

        // For dev:
        args[0] = "bytecode.txt";

        // Initialize variables
        HashMap<String, Register> variables = new HashMap<>();
        ArrayList<Command> commands = new ArrayList<>();
        ArrayList<String> tokens = new ArrayList<>();

        // Read in file and print it out
        File bytecodeFile = new File(args[0]);
        try (Scanner myReader = new Scanner(bytecodeFile)) {
            while (myReader.hasNext()) {
                String data = myReader.next();
                //System.out.println(data);
                tokens.add(data);
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        commands = parseCommands(tokens);
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

        for (Command c : listToReturn) {
            System.out.println(c.toString());
        }

        return listToReturn;
    }
}
