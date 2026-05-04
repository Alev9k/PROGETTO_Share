package mainclass;

import boundary.javafx.*;
import boundary.cli.*;
import model.dao.*;

import java.util.Scanner;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== CONFIGURAZIONE SISTEMA SHARE ===");

        // 1. SCELTA PERSISTENZA
        System.out.println("Scegli modalità persistenza:");
        System.out.println("1. Demo (In-Memory)");
        System.out.println("2. Full (File System)");
        int contextChoice = scanner.nextInt();

        FactoryDAO.Context context = (contextChoice == 1) ?
                FactoryDAO.Context.MEMORY : FactoryDAO.Context.FILE_SYSTEM;

        UserDAO selectedDao = FactoryDAO.getUserDAO(context);

        // 2. SCELTA INTERFACCIA
        System.out.println("\nScegli interfaccia utente:");
        System.out.println("1. CLI (Terminale)");
        System.out.println("2. JavaFX (Grafica Frutiger Aero)");
        int i = scanner.nextInt();

        if (i == 1) {
            new FrontControllerCLI(selectedDao).start();
        } else {
            MainAppGUI.setDAO(selectedDao);
            Application.launch(MainAppGUI.class, args);
        }
    }
}
