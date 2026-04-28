package share.boundary;

import share.controller.LoginController;
import share.controller.RegistrationController;
import share.model.entity.User;
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
        System.out.println("Tipo: 1.Admin, 2.Operator, 3.Technician");
        int t = scanner.nextInt();

        if (regController.register(u, p, t)) {
            System.out.println("Registrazione completata con successo!");
        } else {
            System.out.println("Errore: Username già esistente.");
        }
    }

    private User handleLogin() {
        System.out.print("Username: "); String u = scanner.nextLine();
        System.out.print("Password: "); String p = scanner.nextLine();
        User logged = loginController.login(u, p);
        if (logged == null) System.out.println("Credenziali errate!");
        return logged;
    }
}