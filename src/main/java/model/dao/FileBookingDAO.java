package model.dao;

import exceptions.DAOException;
import model.entity.Booking;
import model.entity.BookingSlot;
import model.entity.ReturnCondition;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FileBookingDAO implements BookingDAO {
    private static final Path DEFAULT_FILE = Path.of("bookings.csv");
    private static final Object FILE_LOCK = new Object();
    private final Path bookingsFile;

    public FileBookingDAO() {
        this(DEFAULT_FILE);
    }

    public FileBookingDAO(Path bookingsFile) {
        this.bookingsFile = Objects.requireNonNull(bookingsFile);
    }

    @Override
    public void save(Booking booking) {
        Objects.requireNonNull(booking);
        synchronized (FILE_LOCK) {
            List<Booking> bookings = findAll();
            if (bookings.stream().anyMatch(existing ->
                    existing.getBookingId().equals(booking.getBookingId()))) {
                throw new DAOException("Esiste già una prenotazione con questo identificativo.");
            }
            BookingDAO.requireNoConflicts(bookings, booking);
            try {
                ensureParentDirectory();
                try (BufferedWriter writer = Files.newBufferedWriter(bookingsFile,
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND)) {
                    writeBooking(writer, booking);
                }
            } catch (IOException e) {
                throw new DAOException("Impossibile salvare la prenotazione.");
            }
        }
    }

    @Override
    public void deleteByIds(Collection<String> bookingIds) {
        Set<String> ids = new HashSet<>(Objects.requireNonNull(bookingIds));
        if (ids.isEmpty()) {
            return;
        }
        synchronized (FILE_LOCK) {
            List<Booking> bookings = findAll();
            long found = bookings.stream()
                    .filter(booking -> ids.contains(booking.getBookingId()))
                    .count();
            if (found != ids.size()) {
                throw new DAOException("Una o più prenotazioni non sono state trovate.");
            }
            bookings.removeIf(booking -> ids.contains(booking.getBookingId()));
            rewrite(bookings);
        }
    }

    @Override
    public void update(Booking updatedBooking) {
        Objects.requireNonNull(updatedBooking);
        synchronized (FILE_LOCK) {
            List<Booking> bookings = findAll();
            boolean found = false;
            for (int i = 0; i < bookings.size(); i++) {
                if (bookings.get(i).getBookingId().equals(updatedBooking.getBookingId())) {
                    bookings.set(i, updatedBooking);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new DAOException("Prenotazione non trovata per l'aggiornamento.");
            }
            rewrite(bookings);
        }
    }

    @Override
    public List<Booking> findAll() {
        if (!Files.exists(bookingsFile)) {
            return new ArrayList<>();
        }
        try {
            List<Booking> bookings = new ArrayList<>();
            for (String line : Files.readAllLines(bookingsFile, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    bookings.add(parseBooking(line));
                }
            }
            return bookings;
        } catch (IOException | RuntimeException e) {
            throw new DAOException("Impossibile leggere le prenotazioni.");
        }
    }

    private Booking parseBooking(String line) {
        List<String> fields = parseCsvLine(line);
        if (fields.size() != 8) {
            throw new IllegalArgumentException("Riga prenotazione non valida.");
        }
        return new Booking(fields.get(0), Integer.parseInt(fields.get(1)),
                Integer.parseInt(fields.get(2)), fields.get(3),
                new BookingSlot(LocalDate.parse(fields.get(4)),
                        LocalTime.parse(fields.get(5)), Integer.parseInt(fields.get(6))),
                ReturnCondition.valueOf(fields.get(7)));
    }

    private void rewrite(List<Booking> bookings) {
        try {
            ensureParentDirectory();
            try (BufferedWriter writer = Files.newBufferedWriter(bookingsFile,
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                for (Booking booking : bookings) {
                    writeBooking(writer, booking);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Impossibile aggiornare le prenotazioni.");
        }
    }

    private void writeBooking(BufferedWriter writer, Booking booking) throws IOException {
        writeCsvRecord(writer, List.of(
                booking.getBookingId(),
                Integer.toString(booking.getGroupId()),
                Integer.toString(booking.getItemId()),
                booking.getOperatorUsername(),
                booking.getDate().toString(),
                booking.getStartTime().toString(),
                Integer.toString(booking.getDurationMinutes()),
                booking.getReturnCondition().name()
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
        int index = 0;
        while (index < line.length()) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length()
                        && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
            index++;
        }
        if (quoted) {
            throw new IllegalArgumentException("Virgolette CSV non bilanciate.");
        }
        fields.add(current.toString());
        return fields;
    }

    private void ensureParentDirectory() throws IOException {
        Path parent = bookingsFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
