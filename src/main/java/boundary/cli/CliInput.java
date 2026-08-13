package boundary.cli;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Lettura e validazione degli input condivisa dalle boundary CLI. */
class CliInput {
    private final Scanner scanner;

    CliInput(Scanner scanner) {
        this.scanner = scanner;
    }

    int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero intero valido.");
            }
        }
    }

    int readChoice(String prompt, int minimum, int maximum) {
        while (true) {
            int choice = readInt(prompt);
            if (choice >= minimum && choice <= maximum) {
                return choice;
            }
            System.out.println("Scelta non valida.");
        }
    }

    String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Il valore non può essere vuoto.");
        }
    }

    LocalTime readTime(String prompt) {
        while (true) {
            String value = readRequired(prompt + " (HH:mm): ");
            try {
                return LocalTime.parse(value);
            } catch (DateTimeParseException e) {
                System.out.println("Orario non valido. Usa il formato HH:mm, ad esempio 08:30.");
            }
        }
    }

    boolean confirm(String prompt) {
        while (true) {
            System.out.print(prompt + " [s/n]: ");
            String answer = scanner.nextLine().trim();
            if (answer.equalsIgnoreCase("s")) {
                return true;
            }
            if (answer.equalsIgnoreCase("n")) {
                return false;
            }
            System.out.println("Rispondi con 's' oppure 'n'.");
        }
    }
}
