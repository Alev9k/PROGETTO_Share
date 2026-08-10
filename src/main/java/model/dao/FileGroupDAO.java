package model.dao;

import exceptions.DAOException;
import exceptions.DuplicateItemNameException;
import model.entity.Group;
import model.entity.Item;
import model.entity.ItemStatus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Persistenza su file dell'aggregato Group e dei relativi item. */
public class FileGroupDAO implements GroupDAO {
    private static final Path DEFAULT_GROUPS_FILE = Path.of("groups.csv");
    private static final Path DEFAULT_ITEMS_FILE = Path.of("items.csv");

    private final Path groupsFile;
    private final Path itemsFile;

    public FileGroupDAO() {
        this(DEFAULT_GROUPS_FILE, DEFAULT_ITEMS_FILE);
    }

    public FileGroupDAO(Path groupsFile, Path itemsFile) {
        this.groupsFile = Objects.requireNonNull(groupsFile);
        this.itemsFile = Objects.requireNonNull(itemsFile);
    }

    @Override
    public List<Group> findAll() {
        Map<Integer, Group> groupsById = new LinkedHashMap<>();
        try {
            if (Files.exists(groupsFile)) {
                for (String line : Files.readAllLines(groupsFile, StandardCharsets.UTF_8)) {
                    if (line.isBlank()) {
                        continue;
                    }
                    Group group = parseGroup(line);
                    groupsById.put(group.getGroupID(), group);
                }
            }
            hydrateItems(groupsById);
            return new ArrayList<>(groupsById.values());
        } catch (IOException | IllegalArgumentException | DuplicateItemNameException e) {
            throw new DAOException("Impossibile leggere gruppi e item dalla persistenza.");
        }
    }

    @Override
    public void save(Group group) {
        Objects.requireNonNull(group, "Il gruppo da salvare è obbligatorio.");
        if (findGroupById(group.getGroupID()) != null) {
            throw new DAOException("Esiste già un gruppo con ID " + group.getGroupID() + ".");
        }

        try {
            ensureParentDirectory(groupsFile);
            try (BufferedWriter writer = Files.newBufferedWriter(groupsFile,
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                writeGroup(writer, group);
            }

            if (!group.getItems().isEmpty()) {
                ensureParentDirectory(itemsFile);
                try (BufferedWriter writer = Files.newBufferedWriter(itemsFile,
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND)) {
                    for (Item item : group.getItems()) {
                        writeItem(writer, item);
                    }
                }
            }
        } catch (IOException e) {
            throw new DAOException("Impossibile salvare il gruppo.");
        }
    }

    @Override
    public void update(Group updatedGroup) {
        Objects.requireNonNull(updatedGroup, "Il gruppo da aggiornare è obbligatorio.");
        List<Group> groups = findAll();
        boolean found = false;
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getGroupID() == updatedGroup.getGroupID()) {
                groups.set(i, updatedGroup);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new DAOException("Gruppo non trovato per l'aggiornamento.");
        }

        try {
            rewriteGroups(groups);
            rewriteItems(groups);
        } catch (IOException e) {
            throw new DAOException("Impossibile aggiornare gruppi e item.");
        }
    }

    @Override
    public Group findGroupById(int id) {
        return findAll().stream()
                .filter(group -> group.getGroupID() == id)
                .findFirst()
                .orElse(null);
    }

    private void hydrateItems(Map<Integer, Group> groupsById)
            throws IOException, DuplicateItemNameException {
        if (!Files.exists(itemsFile)) {
            return;
        }

        for (String line : Files.readAllLines(itemsFile, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            List<String> fields = parseCsvLine(line);
            if (fields.size() != 6) {
                throw new IllegalArgumentException("Riga item non valida.");
            }

            int itemId = Integer.parseInt(fields.get(0));
            int groupId = Integer.parseInt(fields.get(1));
            Group group = groupsById.get(groupId);
            if (group == null) {
                throw new IllegalArgumentException("Item associato a un gruppo inesistente.");
            }

            Item item = new Item(itemId, fields.get(2), groupId,
                    Integer.parseInt(fields.get(3)), Integer.parseInt(fields.get(4)));
            item.setStatus(ItemStatus.valueOf(fields.get(5)));
            group.addItem(item);
        }
    }

    private Group parseGroup(String line) {
        List<String> fields = parseCsvLine(line);
        if (fields.size() != 4 && fields.size() != 6) {
            throw new IllegalArgumentException("Riga gruppo non valida.");
        }

        String accessToken = fields.size() == 6 ? fields.get(4) : "";
        String ownerUsername = fields.size() == 6 ? fields.get(5) : "";
        return new Group(Integer.parseInt(fields.get(0)), fields.get(1),
                LocalTime.parse(fields.get(2)), LocalTime.parse(fields.get(3)),
                accessToken, ownerUsername);
    }

    private void rewriteGroups(List<Group> groups) throws IOException {
        ensureParentDirectory(groupsFile);
        try (BufferedWriter writer = Files.newBufferedWriter(groupsFile,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Group group : groups) {
                writeGroup(writer, group);
            }
        }
    }

    private void rewriteItems(List<Group> groups) throws IOException {
        ensureParentDirectory(itemsFile);
        try (BufferedWriter writer = Files.newBufferedWriter(itemsFile,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Group group : groups) {
                for (Item item : group.getItems()) {
                    writeItem(writer, item);
                }
            }
        }
    }

    private void writeGroup(BufferedWriter writer, Group group) throws IOException {
        writeCsvRecord(writer, List.of(
                Integer.toString(group.getGroupID()),
                group.getName(),
                group.getOpenTime().toString(),
                group.getCloseTime().toString(),
                group.getAccessToken(),
                group.getOwnerUsername()
        ));
    }

    private void writeItem(BufferedWriter writer, Item item) throws IOException {
        writeCsvRecord(writer, List.of(
                Integer.toString(item.getItemID()),
                Integer.toString(item.getGroupID()),
                item.getName(),
                Integer.toString(item.getPriority()),
                Integer.toString(item.getMaxUsageTime()),
                item.getStatus().name()
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

    private void ensureParentDirectory(Path file) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
