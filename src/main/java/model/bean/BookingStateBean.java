package model.bean;

import model.entity.ReturnCondition;

import java.util.Objects;

/** Stato della riconsegna e azioni attualmente consentite su una prenotazione. */
public class BookingStateBean {
    private final boolean deletable;
    private final boolean returnable;
    private final ReturnCondition returnCondition;

    public BookingStateBean(boolean deletable, boolean returnable,
                            ReturnCondition returnCondition) {
        this.deletable = deletable;
        this.returnable = returnable;
        this.returnCondition = Objects.requireNonNull(returnCondition);
    }

    public boolean isDeletable() {
        return deletable;
    }

    public boolean isReturnable() {
        return returnable;
    }

    public ReturnCondition getReturnCondition() {
        return returnCondition;
    }
}
