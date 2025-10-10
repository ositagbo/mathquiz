package com.example.mathquiz.domain;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum Month {
    JANUARY("January", "Jan", 31),
    FEBRUARY("February", "Feb", 28),
    MARCH("March", "Mar", 31),
    APRIL("April", "Apr", 30),
    MAY("May", "May", 31),
    JUNE("June", "Jun", 30),
    JULY("July", "Jul", 31),
    AUGUST("August", "Aug", 31),
    SEPTEMBER("September", "Sep", 30),
    OCTOBER("October", "Oct", 31),
    NOVEMBER("November", "Nov", 30),
    DECEMBER("December", "Dec", 31);

    private final String fullName;
    private final String abbreviation;
    private final int days;

    Month(String fullName, String abbreviation, int days) {
        this.fullName = fullName;
        this.abbreviation = abbreviation;
        this.days = days;
    }

    public int getDays(boolean leapYear) {
        return this == FEBRUARY ? (leapYear ? 29 : 28) : days;
    }

    public static Month fromAbbreviation(String abbrev) {
        return Arrays.stream(values())
                .filter(m -> m.getAbbreviation().equalsIgnoreCase(abbrev))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("Invalid month " + "abbreviation: %s", abbrev))
                );
    }
}
