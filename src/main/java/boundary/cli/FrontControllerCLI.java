package boundary.cli;

import controller.*;
import model.bean.*;
import java.util.Scanner;

public class FrontControllerCLI {
    private final ControllerFactory factory;

    public FrontControllerCLI(ControllerFactory factory) {
        this.factory = factory;
    }

    public void start() {
        Scanner sc = new Scanner(System.in);
        LoginController lc = factory.createLoginController();
        RegistrationController rc = factory.createRegistrationController();

        while (true) {
            try {
                System.out.println("\n--- BENVENUTO IN SHARE ---");
                System.out.println("1. Registrati\n2. Accedi\n3. Esci");
                System.out.print("Scelta: ");

                int choice = Integer.parseInt(sc.nextLine());

                if (choice == 1) {
                    new RegistrationBoundaryCLI(rc, sc).start();
                } else if (choice == 2) {
                    UserBean loggedUser = new LoginBoundaryCLI(lc, sc).start();
                    if (loggedUser != null) {
                        dispatchUser(loggedUser, sc);
                    }
                } else {
                    System.out.println("Arrivederci!");
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero valido.");
            }
        }
    }

    private void dispatchUser(UserBean user, Scanner sc) {
        System.out.println("\nAccesso effettuato come: " + user.getUsername());

        switch (user.getRole()) {
            case ADMIN:
                showAdminMenu(user, sc);
                break;

            case OPERATOR:
                System.out.println("[MENU OPERATORE] Funzionalità non ancora disponibili.");
                break;

            case TECHNICIAN:
                System.out.println("[MENU TECNICO] Funzionalità non ancora disponibili.");
                break;

            default:
                System.out.println("Errore: Ruolo non riconosciuto.");
                break;
        }
    }

    /**
     * Sotto-menu dedicato alle funzionalità dell'Admin.
     */
    private void showAdminMenu(UserBean user, Scanner sc) {
        boolean exitAdmin = false;

        while (!exitAdmin) {
            System.out.println("\n--- MENU AMMINISTRATORE ---");
            System.out.println("1. Crea Nuovo Gruppo");
            System.out.println("2. Gestisci Gruppi Esistenti");
            System.out.println("0. Logout");
            System.out.print("Scelta: ");

            try {
                int choice = Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 1:
                        System.out.println("\n[!] Funzionalità 'Crea Gruppo' non ancora implementata.");
                        // Resta nel ciclo, quindi torna al menu Admin
                        break;

                    case 2:
                        // Avviamo il caso d'uso già implementato
                        ManageGroupController mgc = factory.createManageGroupController();
                        new ManageGroupBoundaryCLI(mgc, factory, user.getUsername(), sc).showMenu();
                        break;

                    case 0:
                        System.out.println("Effettuo logout...");
                        exitAdmin = true;
                        break;

                    default:
                        System.out.println("Scelta non valida.");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Per favore, inserisci un numero.");
            }
        }
    }
}
