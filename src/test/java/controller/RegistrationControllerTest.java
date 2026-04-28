package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import model.dao.InMemoryUserDAO;
import exceptions.UserAlreadyExistsException;
import model.entity.User;

/**
 * Studente in carica: [IL TUO NOME E COGNOME]
 * Test case richiesto per soddisfare i requisiti di qualità e testing del progetto.
 */
class RegistrationControllerTest {

    private RegistrationController registrationController;
    private InMemoryUserDAO inMemoryDao;

    @BeforeEach
    void setUp() {
        // Usiamo la versione In-Memory come richiesto per la modalità demo
        inMemoryDao = new InMemoryUserDAO();
        registrationController = new RegistrationController(inMemoryDao);
    }

    @Test
    void testRegistrazioneSuccesso() throws UserAlreadyExistsException {
        // Esecuzione della registrazione di un Admin (Tipo 1)
        registrationController.register("NuovoUtente", "password123", 1);

        // Verifica che l'utente sia stato effettivamente salvato nel DAO
        User savedUser = inMemoryDao.findByUsername("NuovoUtente");
        assertNotNull(savedUser, "L'utente dovrebbe essere salvato nel DAO");
        assertEquals("NuovoUtente", savedUser.getUsername());
    }

    @Test
    void testRegistrazioneDuplicataLanciaEccezione() throws UserAlreadyExistsException {
        // Registriamo un primo utente
        registrationController.register("UtenteEsistente", "pass1", 1);

        // Verifichiamo che il tentativo di registrare lo stesso nome lanci la nostra eccezione custom
        assertThrows(UserAlreadyExistsException.class, () -> {
            registrationController.register("UtenteEsistente", "pass2", 2);
        }, "Dovrebbe lanciare UserAlreadyExistsException se lo username è già preso");
    }
}