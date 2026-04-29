package boundary.CLI;

import exceptions.InvalidCredentialsException;
import controller.LoginController;
import model.entity.User;
import java.util.Scanner;

public class LoginBoundaryCLI {
    private final LoginController loginController;
    private final Scanner scanner;

    public LoginBoundaryCLI(LoginController lc, Scanner scanner) {
        this.loginController = lc;
        this.scanner = scanner;
    }

    public User start() {
        System.out.println("\n--- ACCESSO (LOGIN) ---");
        System.out.print("Username: ");
        String u = scanner.nextLine();
        System.out.print("Password: ");
        String p = scanner.nextLine();

        try {
            User logged = loginController.login(u, p);
            System.out.println(">>> Accesso eseguito. Benvenuto " + logged.getUsername() + "!");
            return logged;
        } catch (InvalidCredentialsException e) {
            System.err.println("ERRORE DI AUTENTICAZIONE: " + e.getMessage());
            return null; // Segnala al chiamante che il login è fallito
        }
    }
}