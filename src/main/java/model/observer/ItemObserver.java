package model.observer;

/** Subscriber interessato alla transizione di un item allo stato BROKEN. */
public interface ItemObserver {
    void onItemBroken(ItemBrokenEvent event);
}
