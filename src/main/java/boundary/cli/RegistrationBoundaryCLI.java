package boundary.cli;

import controller.RegistrationController;
import exceptions.UserAlreadyExistsException;
import java.util.Scanner;

public class RegistrationBoundaryCLI {
    private final RegistrationController regController;
    private final CliInput input;

    public RegistrationBoundaryCLI(RegistrationController rc, Scanner scanner) {
        this.regController = rc;
        this.input = new CliInput(scanner);
    }

    public void start() {
        System.out.println("\n--- REGISTRAZIONE NUOVO ACCOUNT ---");
        String u = input.readRequired("Nuovo username: ");
        String p = input.readRequired("Nuova password: ");

        System.out.println("Scegli il tipo di utente:");
        System.out.println("1. Admin");
        System.out.println("2. Operator");
        int t = input.readChoice("Scelta: ", 1, 2);

        try {
            regController.register(u, p, t);
            System.out.println(">>> Registrazione completata con successo!");
        } catch (UserAlreadyExistsException e) {
            System.err.println("ERRORE: " + e.getMessage());
        }
    }
}
