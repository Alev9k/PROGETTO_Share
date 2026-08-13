package controller.observer;

import model.dao.BookingDAO;
import model.dao.GroupDAO;
import model.dao.NotificationDAO;
import model.entity.Booking;
import model.entity.Group;
import model.entity.Notification;
import model.entity.NotificationType;
import model.observer.ItemBrokenEvent;
import model.observer.ItemObserver;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Invalida le prenotazioni future dell'item e avvisa gli operatori coinvolti. */
public class FutureBookingsCancellationObserver implements ItemObserver {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final BookingDAO bookingDAO;
    private final GroupDAO groupDAO;
    private final NotificationDAO notificationDAO;

    public FutureBookingsCancellationObserver(BookingDAO bookingDAO, GroupDAO groupDAO,
                                              NotificationDAO notificationDAO) {
        this.bookingDAO = Objects.requireNonNull(bookingDAO);
        this.groupDAO = Objects.requireNonNull(groupDAO);
        this.notificationDAO = Objects.requireNonNull(notificationDAO);
    }

    @Override
    public void onItemBroken(ItemBrokenEvent event) {
        List<Booking> futureBookings = bookingDAO.findAll().stream()
                .filter(booking -> booking.getGroupId() == event.groupId())
                .filter(booking -> booking.getItemId() == event.itemId())
                .filter(booking -> booking.startsAtOrAfter(event.reportedAt()))
                .toList();
        if (futureBookings.isEmpty()) {
            return;
        }

        bookingDAO.deleteByIds(futureBookings.stream()
                .map(Booking::getBookingId)
                .toList());

        Group group = groupDAO.findGroupById(event.groupId());
        String groupName = group == null ? "Gruppo " + event.groupId() : group.getName();
        List<Notification> notifications = futureBookings.stream()
                .map(booking -> cancellationNotification(booking, event, groupName))
                .toList();
        notificationDAO.saveAll(notifications);
    }

    private Notification cancellationNotification(Booking booking,
                                                  ItemBrokenEvent event,
                                                  String groupName) {
        String message = "La prenotazione dell'item '" + event.itemName()
                + "' nel gruppo '" + groupName + "' del "
                + DATE_FORMAT.format(booking.getDate()) + " alle "
                + TIME_FORMAT.format(booking.getStartTime())
                + " è stata cancellata perché l'item è guasto.";
        return new Notification(UUID.randomUUID().toString(),
                booking.getOperatorUsername(), NotificationType.BOOKING_CANCELLED,
                message, event.reportedAt());
    }
}
