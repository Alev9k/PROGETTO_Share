package mainclass;

import boundary.cli.*;
import controller.ControllerFactory;
import model.dao.*;
import boundary.javafx.*;
import javafx.application.Application;
import model.session.UserSession;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== CONFIGURAZIONE SISTEMA SHARE ===");

        // 1. SCELTA PERSISTENZA
        System.out.println("Scegli modalità persistenza:");
        System.out.println("1. Demo (In-Memory)");
        System.out.println("2. Full (File System)");
        int contextChoice = scanner.nextInt();
        scanner.nextLine(); // Pulizia buffer

        FactoryDAO.Context context = (contextChoice == 1) ?
                FactoryDAO.Context.MEMORY : FactoryDAO.Context.FILE_SYSTEM;

        // Recuperiamo entrambi i DAO necessari per il sistema
        UserDAO userDAO = FactoryDAO.getUserDAO(context);
        GroupDAO groupDAO = FactoryDAO.getGroupDAO(context);
        MembershipRequestDAO membershipRequestDAO = FactoryDAO.getMembershipRequestDAO(context);
        BookingDAO bookingDAO = FactoryDAO.getBookingDAO(context);
        NotificationDAO notificationDAO = FactoryDAO.getNotificationDAO(context);

        // 2. CREAZIONE DELLA CONTROLLER FACTORY
        // Centralizziamo qui la gestione delle dipendenze
        UserSession userSession = UserSession.getInstance();
        ControllerFactory controllerFactory = new ControllerFactory(
                userDAO, groupDAO, membershipRequestDAO, bookingDAO, notificationDAO,
                userSession);

        // 3. SCELTA INTERFACCIA
        System.out.println("\nScegli interfaccia utente:");
        System.out.println("1. CLI (Terminale)");
        System.out.println("2. JavaFX (Grafica)");
        int interfaceChoice = scanner.nextInt();
        scanner.nextLine(); // Pulizia buffer

        if (interfaceChoice == 1) {
            // Passiamo la factory al Front Controller CLI
            new FrontControllerCLI(controllerFactory, userSession).start();
        } else {
            // Per JavaFX, potresti passare la factory tramite un setter o costruttore
            MainAppGUI.setControllerFactory(controllerFactory);
            Application.launch(MainAppGUI.class, args);
        }
    }
}
