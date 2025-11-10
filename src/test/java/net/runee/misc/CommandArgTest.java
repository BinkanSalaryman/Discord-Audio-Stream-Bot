package net.runee.misc;

import org.junit.jupiter.api.Test;
import java.util.Scanner;

public class CommandArgTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter command args:");
            String input = scanner.nextLine();
            if (input.equals("exit")) {
                break;
            }
            String[] cmdArgs = Utils.parseCommandArgs(input);
            for (int i = 0; i < cmdArgs.length; i++) {
                System.out.println(" * arg " + i + ": '" + cmdArgs[i] + "'");
            }
        }
    }

    @Test
    void __dummy__ () {
        // make gradle happy when doesn't find any test (use above main function to test manually)
    }
}
