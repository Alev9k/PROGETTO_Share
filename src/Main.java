import share.boundary.*;
import share.model.entity.*;

public class Main {
    public static void main(String[] args) {
        LoginBoundary loginBoundary = new LoginBoundary();

        // 1. Fase di Autenticazione
        User loggedUser = loginBoundary.start();

        if (loggedUser == null) {
            System.out.println("Accesso negato.");
            return;
        }

        // 2. Smistamento in base al ruolo (Generalizzazione/Polimorfismo)
        if (loggedUser instanceof Admin) {
            System.out.println("Benvenuto Admin " + loggedUser.getUsername());
            ManageGroupBoundary adminUI = new ManageGroupBoundary();
            //adminUI.start((Admin) loggedUser); // Cast all'oggetto specifico
        }
        else if (loggedUser instanceof Operator) {
            System.out.println("Benvenuto Operatore " + loggedUser.getUsername());
            // Qui andrebbe il boundary dell'operatore per prenotare
        }
        else if (loggedUser instanceof Technician) {
            System.out.println("Benvenuto Tecnico " + loggedUser.getUsername());
            // Qui andrebbe il boundary del tecnico per prenotare
        }
    }
}
