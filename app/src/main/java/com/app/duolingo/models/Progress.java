package com.app.duolingo.models;

public class Progress {
    private String id;
    private double learnProgress;
    private double reviewProgress;
    private String userId;
    private String courseId;

    // Constructor
    public Progress(String id, double learnProgress, double reviewProgress, String userId, String courseId) {
        this.id = id;
        this.learnProgress = learnProgress;
        this.reviewProgress = reviewProgress;
        this.userId = userId;
        this.courseId = courseId;
    }

    // Default constructor
    public Progress() {
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLearnProgress() {
        return learnProgress;
    }

    public void setLearnProgress(double learnProgress) {
        this.learnProgress = learnProgress;
    }

    public double getReviewProgress() {
        return reviewProgress;
    }

    public void setReviewProgress(double reviewProgress) {
        this.reviewProgress = reviewProgress;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
}
