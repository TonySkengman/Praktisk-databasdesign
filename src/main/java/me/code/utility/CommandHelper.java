package me.code.utility;

import java.util.Scanner;
import java.util.UUID;

public class CommandHelper {

    public static UUID queryTransactionId() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter ID of transaction: ");

        try {
            String id = scanner.nextLine();
            return UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            System.out.println("The id must be a valid UUID.");
            return null;
        }
    }

}
