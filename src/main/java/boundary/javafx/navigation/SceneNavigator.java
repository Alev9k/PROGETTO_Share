package boundary.javafx.navigation;

import model.bean.GroupBean;
import model.bean.UserBean;

/** Contratto di navigazione esposto alle boundary JavaFX. */
public interface SceneNavigator {
    void showLogin();
    void showRegistration();
    void showDashboard(UserBean user);
    void showCreateGroup(String adminUsername);
    void showManageGroups(String adminUsername);
    void showManageItems(GroupBean group, String adminUsername);
    void showManageOperators(GroupBean group, String adminUsername);
    void showRequestGroupAccess(String operatorUsername);
    void showMyGroups(String operatorUsername);
}
