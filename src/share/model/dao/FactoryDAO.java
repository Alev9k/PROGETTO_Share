package share.model.dao;

public class FactoryDAO {
    public enum Context { MEMORY, FILE_SYSTEM }

    public static UserDAO getUserDAO(Context context) {
        return switch (context) {
            case MEMORY -> new InMemoryUserDAO();
            case FILE_SYSTEM -> new FileUserDAO();
        };
    }
}