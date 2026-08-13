package boundary.javafx.navigation;

import model.bean.GroupBean;

/** Contratto di navigazione esposto alle boundary JavaFX. */
public interface SceneNavigator {
    void showLogin();
    void showRegistration();
    void showDashboard();
    void showCreateGroup();
    void showManageGroups();
    void showManageItems(GroupBean group);
    void showManageOperators(GroupBean group);
    void showRequestGroupAccess();
    void showMyGroups();
    void showMyBookings();
    void logout();
}
