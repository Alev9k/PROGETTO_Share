package model.dao;

import exceptions.DAOException;
import model.entity.Notification;
import model.entity.NotificationType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FileNotificationDAO implements NotificationDAO {
    private static final Path DEFAULT_FILE = Path.of("notifications.csv");
    private static final Object FILE_LOCK = new Object();
    private final Path notificationsFile;

    public FileNotificationDAO() {
        this(DEFAULT_FILE);
    }

    public FileNotificationDAO(Path notificationsFile) {
        this.notificationsFile = Objects.requireNonNull(notificationsFile);
    }

    @Override
    public void saveAll(Collection<Notification> newNotifications) {
        Objects.requireNonNull(newNotifications);
        if (newNotifications.isEmpty()) {
            return;
        }
        synchronized (FILE_LOCK) {
            List<Notification> existing = findAll();
            Set<String> ids = new HashSet<>();
            existing.forEach(notification -> ids.add(notification.getNotificationId()));
            for (Notification notification : newNotifications) {
                Objects.requireNonNull(notification);
                if (!ids.add(notification.getNotificationId())) {
                    throw new DAOException("Esiste già una notifica con questo identificativo.");
                }
            }
            try {
                ensureParentDirectory();
                try (BufferedWriter writer = Files.newBufferedWriter(notificationsFile,
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND)) {
                    for (Notification notification : newNotifications) {
                        writeNotification(writer, notification);
                    }
                }
            } catch (IOException e) {
                throw new DAOException("Impossibile salvare le notifiche.");
            }
        }
    }

    @Override
    public void update(Notification updatedNotification) {
        Objects.requireNonNull(updatedNotification);
        synchronized (FILE_LOCK) {
            List<Notification> notifications = findAll();
            boolean found = false;
            for (int i = 0; i < notifications.size(); i++) {
                if (notifications.get(i).getNotificationId()
                        .equals(updatedNotification.getNotificationId())) {
                    notifications.set(i, updatedNotification);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new DAOException("Notifica non trovata per l'aggiornamento.");
            }
            rewrite(notifications);
        }
    }

    @Override
    public List<Notification> findAll() {
        if (!Files.exists(notificationsFile)) {
            return new ArrayList<>();
        }
        try {
            List<Notification> notifications = new ArrayList<>();
            for (String line : Files.readAllLines(notificationsFile, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    notifications.add(parseNotification(line));
                }
            }
            return notifications;
        } catch (IOException | RuntimeException e) {
            throw new DAOException("Impossibile leggere le notifiche.");
        }
    }

    private Notification parseNotification(String line) {
        List<String> fields = parseCsvLine(line);
        if (fields.size() != 6) {
            throw new IllegalArgumentException("Riga notifica non valida.");
        }
        return new Notification(fields.get(0), fields.get(1),
                NotificationType.valueOf(fields.get(2)), fields.get(3),
                LocalDateTime.parse(fields.get(4)), Boolean.parseBoolean(fields.get(5)));
    }

    private void rewrite(List<Notification> notifications) {
        try {
            ensureParentDirectory();
            try (BufferedWriter writer = Files.newBufferedWriter(notificationsFile,
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                for (Notification notification : notifications) {
                    writeNotification(writer, notification);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Impossibile aggiornare le notifiche.");
        }
    }

    private void writeNotification(BufferedWriter writer, Notification notification)
            throws IOException {
        writeCsvRecord(writer, List.of(
                notification.getNotificationId(),
                notification.getRecipientUsername(),
                notification.getType().name(),
                notification.getMessage(),
                notification.getCreatedAt().toString(),
                Boolean.toString(notification.isRead())
        ));
    }

    private void writeCsvRecord(BufferedWriter writer, List<String> fields) throws IOException {
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                writer.write(',');
            }
            writer.write(escapeCsv(fields.get(i)));
        }
        writer.newLine();
    }

    private String escapeCsv(String value) {
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Virgolette CSV non bilanciate.");
        }
        fields.add(current.toString());
        return fields;
    }

    private void ensureParentDirectory() throws IOException {
        Path parent = notificationsFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
