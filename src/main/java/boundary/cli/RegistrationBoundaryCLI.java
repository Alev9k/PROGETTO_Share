package boundary.cli;

import controller.RegistrationController;
import exceptions.UserAlreadyExistsException;
import java.util.Scanner;

public class RegistrationBoundaryCLI {
    private final RegistrationController regController;
    private final Scanner scanner;

    public RegistrationBoundaryCLI(RegistrationController rc, Scanner scanner) {
        this.regController = rc;
        this.scanner = scanner;
    }

    public void start() {
        System.out.println("\n--- REGISTRAZIONE NUOVO ACCOUNT ---");
        System.out.print("Nuovo Username: ");
        String u = scanner.nextLine();
        System.out.print("Nuova Password: ");
        String p = scanner.nextLine();

        System.out.println("Scegli il tipo di utente:");
        System.out.println("1. Admin");
        System.out.println("2. Operator");
        System.out.println("3. Technician");
        System.out.print("Scelta: ");

        int t = scanner.nextInt();
        scanner.nextLine(); // Consuma il newline rimasto nel buffer

        try {
            regController.register(u, p, t);
            System.out.println(">>> Registrazione completata con successo!");
        } catch (UserAlreadyExistsException e) {
            System.err.println("ERRORE: " + e.getMessage());
        }
    }
}
