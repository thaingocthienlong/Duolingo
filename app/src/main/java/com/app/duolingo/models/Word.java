package com.app.duolingo.models;

public class Word {
    private String id;
    private String english;
    private String meaning;
    private String pronounce;
    private String sound;  // Assuming this is a URL or file path

    // Constructor
    public Word(String id, String english, String meaning, String pronounce, String sound) {
        this.id = id;
        this.english = english;
        this.meaning = meaning;
        this.pronounce = pronounce;
        this.sound = sound;
    }

    // Default constructor
    public Word() {
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getPronounce() {
        return pronounce;
    }

    public void setPronounce(String pronounce) {
        this.pronounce = pronounce;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }
}
