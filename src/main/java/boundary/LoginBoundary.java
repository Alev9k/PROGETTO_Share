package boundary;

import exceptions.InvalidCredentialsException;
import exceptions.UserAlreadyExistsException;
import controller.LoginController;
import controller.RegistrationController;
import model.entity.User;
import java.util.Scanner;

public class LoginBoundary {
    private RegistrationController regController;
    private LoginController loginController;
    private Scanner scanner = new Scanner(System.in);

    public LoginBoundary(RegistrationController rc, LoginController lc) {
        this.regController = rc;
        this.loginController = lc;
    }

    public User start() {
        while (true) {
            System.out.println("--- BENVENUTO ---");
            System.out.println("1. Non sei registrato? Crea account");
            System.out.println("2. Sei già registrato? Accedi");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                handleRegistration(); // Resta nel loop per permettere il login dopo
            } else {
                User u = handleLogin();
                if (u != null) return u; // Esce solo se il login ha successo
            }
        }
    }

    private void handleRegistration() {
        System.out.print("Nuovo Username: "); String u = scanner.nextLine();
        System.out.print("Nuova Password: "); String p = scanner.nextLine();
        System.out.println("Tipo: 1.model.factory.entity.Admin, 2.model.factory.entity.Operator, 3.model.factory.entity.Technician");
        int t = scanner.nextInt();

        try {
            regController.register(u, p, t);
            System.out.println("Registrazione OK!");
        } catch (UserAlreadyExistsException e) {
            // Gestione dell'errore specifica
            System.out.println("ATTENZIONE: " + e.getMessage());
        }
    }

    private User handleLogin() {
        System.out.print("Username: "); String u = scanner.nextLine();
        System.out.print("Password: "); String p = scanner.nextLine();
        try {
            // Chiamata al controller che ora lancia l'eccezione invece di restituire null
            User logged = loginController.login(u, p);
            System.out.println("Accesso eseguito con successo!");
            return logged;

        } catch (InvalidCredentialsException e) {
            // Gestione dell'eccezione custom
            // Stampiamo il messaggio d'errore definito nella classe Exception
            System.out.println("ERRORE DI AUTENTICAZIONE: " + e.getMessage());

            // Restituiamo null per segnalare al metodo start() che il login è fallito
            // e permettergli di mostrare nuovamente il menu principale.
            return null;
        }
    }
}