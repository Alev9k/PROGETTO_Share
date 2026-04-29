package boundary.CLI;

import controller.LoginController;
import controller.RegistrationController;
import model.dao.UserDAO;

import java.util.Scanner;

public class FrontControllerCLI {
    private final UserDAO dao;

    public FrontControllerCLI(UserDAO selectedDAO) { this.dao = selectedDAO; }

    public void start() {
        Scanner sc = new Scanner(System.in);
        LoginController lc = new LoginController(dao);
        RegistrationController rc = new RegistrationController(dao);

        while (true) {
            System.out.println("\n[MENU PRINCIPALE]");
            System.out.println("1. Registrati\n2. Accedi\n3. Esci");
            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1) new RegistrationBoundaryCLI(rc, sc).start();
            else if (choice == 2) {
                if (new LoginBoundaryCLI(lc, sc).start() != null) break; // Entra nell'app
            }
            else break;
        }
    }
}