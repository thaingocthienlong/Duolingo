package com.app.duolingo.models;

import java.util.List;

public class QuizQuestion {
    private String question;
    private List<String> options;
    private String correctOption;

    // Constructor
    public QuizQuestion(String question, List<String> options, String correctOption) {
        this.question = question;
        this.options = options;
        this.correctOption = correctOption;
    }

    // Getters and setters
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }
}
