import java.io.*;
import java.util.Scanner;

public class Interpreter {

    public static void main(String[] args) {
        if (!(args.length > 0)) {
            System.out.println("Usage: java Interpreter <bytecode_file>");
            return;
        }

        // For dev:
        args[0] = "bytecode.txt";

        // Read in file and print it out
        File bytecodeFile = new File(args[0]);
        try (Scanner myReader = new Scanner(bytecodeFile)) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                System.out.println(data);
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
