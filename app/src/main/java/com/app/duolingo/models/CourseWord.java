package com.app.duolingo.models;

public class CourseWord {
    private String id;
    private String courseId;
    private String wordId;

    // Constructor
    public CourseWord(String id, String courseId, String wordId) {
        this.id = id;
        this.courseId = courseId;
        this.wordId = wordId;
    }

    // Default constructor
    public CourseWord() {
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getWordId() {
        return wordId;
    }

    public void setWordId(String wordId) {
        this.wordId = wordId;
    }
}

