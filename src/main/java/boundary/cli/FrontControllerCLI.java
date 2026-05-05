package boundary.cli;

import controller.*;
import model.dao.*;
import model.bean.*;
import java.util.Scanner;

public class FrontControllerCLI {
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    public FrontControllerCLI(UserDAO selectedUserDAO, GroupDAO selectedGroupDAO) {
        this.userDAO = selectedUserDAO;
        this.groupDAO = selectedGroupDAO;
    }

    public void start() {
        Scanner sc = new Scanner(System.in);
        LoginController lc = new LoginController(userDAO);
        RegistrationController rc = new RegistrationController(userDAO);

        while (true) {
            System.out.println("\n--- BENVENUTO IN SHARE ---");
            System.out.println("1. Registrati\n2. Accedi\n3. Esci");
            System.out.print("Scelta: ");
            int choice = sc.nextInt();
            sc.nextLine(); // Pulisce il buffer

            if (choice == 1) {
                new RegistrationBoundaryCLI(rc, sc).start();
            } else if (choice == 2) {
                // Il login ora restituisce un Bean dell'utente loggato
                UserBean loggedUser = new LoginBoundaryCLI(lc, sc).start();

                if (loggedUser != null) {
                    // Se il login ha successo, smistiamo l'utente in base al tipo
                    dispatchUser(loggedUser, sc);
                }
            } else {
                System.out.println("Arrivederci!");
                break;
            }
        }
    }

    /**
     * Metodo privato per indirizzare l'utente al menu corretto in base al ruolo.
     */
    private void dispatchUser(UserBean user, Scanner sc) {
        System.out.println("\nAccesso effettuato come: " + user.getUsername());

        // Verifichiamo il ruolo dell'utente contenuto nel Bean
        switch (user.getRole()) {
            case ADMIN:
                // Avviamo il caso d'uso Gestisci Gruppo che abbiamo implementato
                ManageGroupController mgc = new ManageGroupController(userDAO, groupDAO);
                new ManageGroupBoundaryCLI(mgc, user.getUsername()).showMenu();
                break;

            case OPERATOR:
                // Placeholder per i futuri use case dell'operatore
                System.out.println("[MENU OPERATORE] Funzionalità non ancora disponibili.");
                break;

            case TECHNICIAN:
                // Placeholder per i futuri use case del tecnico
                System.out.println("[MENU TECNICO] Funzionalità non ancora disponibili.");
                break;

            default:
                System.out.println("Errore: Ruolo non riconosciuto.");
                break;
        }
    }
}