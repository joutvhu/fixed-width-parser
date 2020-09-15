package com.joutvhu.fixedwidth.parser.model;

public class Padding {
    public static final Padding DEFAULT = new Padding();

    private Character character;

    private Padding() {
        this.character = null;
    }

    private Padding(char character) {
        this.character = character;
    }

    public static Padding of(char character) {
        return new Padding(character);
    }

    public Character getCharacter() {
        return character;
    }
}
