package com.ControlCards.ControlCards.Util.Enums;

import lombok.Getter;

@Getter
public enum Shift {
    FIRST("Първа"),
    SECOND("Втора"),
    THIRD("Трета");

    private final String displayName;

    Shift(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
