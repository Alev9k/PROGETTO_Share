package main;

import share.controller.*;
import share.model.dao.*;
import share.boundary.*;
import share.model.entity.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 1. SCELTA DEL CONTESTO A RUNTIME
        System.out.println("Seleziona modalità persistenza:\n1. In Memoria (Test)\n2. File System (CSV)");
        int contextChoice = scanner.nextInt();

        FactoryDAO.Context context = (contextChoice == 1) ?
                FactoryDAO.Context.MEMORY : FactoryDAO.Context.FILE_SYSTEM;

        UserDAO selectedDao = FactoryDAO.getUserDAO(context);

        // 2. ISTANZIAMO I DUE CONTROLLER SEPARATI
        RegistrationController regController = new RegistrationController(selectedDao);
        LoginController loginController = new LoginController(selectedDao);

        // 3. PASSIAMO TUTTO AL BOUNDARY
        LoginBoundary boundary = new LoginBoundary(regController, loginController);

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
