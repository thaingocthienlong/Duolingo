package com.app.duolingo.models;

public class Point {
    private String id;
    private int point;
    private String userId;
    private String courseId;

    // Constructor
    public Point(String id, int point, String userId, String courseId) {
        this.id = id;
        this.point = point;
        this.userId = userId;
        this.courseId = courseId;
    }

    public Point() {
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
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

