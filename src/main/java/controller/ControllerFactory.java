package controller;

import model.dao.*;

public class ControllerFactory {
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;

    public ControllerFactory(UserDAO userDAO, GroupDAO groupDAO) {
        this.userDAO = userDAO;
        this.groupDAO = groupDAO;
    }

    // Nuovi metodi per il FrontController
    public LoginController createLoginController() {
        return new LoginController(userDAO);
    }

    public RegistrationController createRegistrationController() {
        return new RegistrationController(userDAO);
    }

    public ManageGroupController createManageGroupController() {
        return new ManageGroupController(userDAO, groupDAO);
    }

    // Metodi per le sotto-boundary (già definiti)
    public ManageOperatorsController createManageOperatorsController(int groupID) throws Exception {
        return new ManageOperatorsController(groupID, userDAO, groupDAO);
    }

    public ManageItemsController createManageItemsController(int groupID) throws Exception {
        return new ManageItemsController(groupID, groupDAO, userDAO);
    }

    public CreateGroupController createCreateGroupController() throws Exception {
        return new CreateGroupController(groupDAO, userDAO);
    }
}