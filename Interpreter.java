import java.io.*;
import java.util.*;
import sun.awt.www.content.audio.wav;

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
                System.out.println(data);
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

        for (int i = 0; i < tokens.size(); i++) {}

        return listToReturn;
    }
}
