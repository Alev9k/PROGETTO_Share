package boundary.cli;

import exceptions.InvalidCredentialsException;
import controller.LoginController;
import java.util.Scanner;
import model.bean.*;

public class LoginBoundaryCLI {
    private final LoginController loginController;
    private final Scanner scanner;

    public LoginBoundaryCLI(LoginController lc, Scanner scanner) {
        this.loginController = lc;
        this.scanner = scanner;
    }

    public boolean start() {
        System.out.println("\n--- ACCESSO (LOGIN) ---");
        System.out.print("Username: ");
        String u = scanner.nextLine();
        System.out.print("Password: ");
        String p = scanner.nextLine();

        try {
            UserBean logged = loginController.login(u, p);
            System.out.println(">>> Accesso eseguito. Benvenuto " + logged.getUsername() + "!");
            return true;
        } catch (InvalidCredentialsException e) {
            System.err.println("ERRORE DI AUTENTICAZIONE: " + e.getMessage());
            return false;
        }
    }
}
