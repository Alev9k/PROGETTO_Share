package controller;

import model.dao.GroupDAO;
import model.dao.UserDAO;
import model.entity.Group;
import model.entity.Operator;
import model.entity.User;
import exceptions.DAOException;

public class JoinGroupController {

    private final GroupDAO groupDAO;
    private final UserDAO userDAO;

    public JoinGroupController(GroupDAO groupDAO, UserDAO userDAO) {
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
    }

    /**
     * Permette a un utente di unirsi a un gruppo usando ID e Nome come "Token a 2 fattori".
     */
    public void joinGroup(String username, int groupId, String groupName) throws Exception {

        // 1. Ricerca del gruppo tramite il primo parametro (L'ID)
        Group targetGroup = groupDAO.findGroupById(groupId);

        if (targetGroup == null) {
            throw new Exception("Nessun gruppo trovato con questo ID.");
        }

        // 2. LA TUA LOGICA DI SICUREZZA: Verifica del secondo parametro (Il Nome)
        // Usiamo equalsIgnoreCase così se l'utente digita "progetto" invece di "Progetto" entra lo stesso
        if (!targetGroup.getName().equalsIgnoreCase(groupName.trim())) {
            throw new Exception("Accesso negato: Le credenziali del gruppo (Nome o ID) non corrispondono.");
        }

        // 3. Recupero dell'utente
        User user = userDAO.findByUsername(username);
        if (!(user instanceof Operator)) {
            // Nota: sia Operator che Technician ereditano/sono gestiti come operatori nel tuo dominio?
            // Adatta questo controllo in base alla tua gerarchia delle classi utente!
            throw new Exception("Solo gli Operatori o i Tecnici possono unirsi ai gruppi.");
        }

        Operator operator = (Operator) user;

        /* 4. Controllo duplicati: L'utente è già nel gruppo?
        for (Group g : operator.getGroups()) { // Supponendo che Operator abbia un metodo getGroups()
            if (g.getGroupID() == groupId) {
                throw new Exception("Sei già iscritto a questo gruppo.");
            }
        }*/

        // 5. Iscrizione effettiva e salvataggio
        // Aggiungiamo l'operatore alla lista del gruppo
        targetGroup.getOperators().add(operator);
        groupDAO.update(targetGroup); // Aggiorniamo il file groups.csv

        // E aggiungiamo il gruppo alla lista dell'operatore (relazione bidirezionale)
        // operator.addGroup(targetGroup);
        // userDAO.updateUser(operator); // Aggiorniamo il file users.csv
    }
}