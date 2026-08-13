package model.observer;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Subject del pattern Observer; i subscriber sono esclusivamente runtime. */
public abstract class ItemSubject {
    private final Set<ItemObserver> observers = new LinkedHashSet<>();

    public final void attach(ItemObserver observer) {
        observers.add(Objects.requireNonNull(observer));
    }

    public final void detach(ItemObserver observer) {
        observers.remove(observer);
    }

    protected final void notifyItemBroken(ItemBrokenEvent event) {
        for (ItemObserver observer : Set.copyOf(observers)) {
            observer.onItemBroken(event);
        }
    }
}
