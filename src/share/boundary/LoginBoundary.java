// File: it.share.boundary.LoginBoundary.java
package share.boundary;

import share.controller.LoginController;
import share.model.entity.User;
import java.util.Scanner;

public class LoginBoundary {
    private LoginController controller = new LoginController();
    private Scanner scanner = new Scanner(System.in);

    public User start() {
        System.out.println("1. Registrati\n2. Login");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) return handleRegister();
        return handleLogin();
    }

    private User handleRegister() {
        System.out.print("Username: "); String u = scanner.nextLine();
        System.out.print("Password: "); String p = scanner.nextLine();
        System.out.println("Tipo: 1.Admin, 2.Operator");
        int t = scanner.nextInt();

        if (controller.register(u, p, t)) {
            System.out.println("Registrazione OK! Effettua il login.");
            return handleLogin();
        }
        System.out.println("Errore registrazione.");
        return null;
    }

    private User handleLogin() {
        System.out.print("Username: "); String u = scanner.nextLine();
        System.out.print("Password: "); String p = scanner.nextLine();
        return controller.login(u, p);
    }
}