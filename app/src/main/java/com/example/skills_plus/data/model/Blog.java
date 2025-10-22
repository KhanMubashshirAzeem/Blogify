package com.example.skills_plus.data.model;


public class Blog {
    private String authorId;
    private String blogId;
    private String title;
    private String description;
    private String image;
    private String timeStamp;

    public Blog() {
        // Default constructor for Firebase
    }

    public Blog(String authorId, String blogId, String title, String description, String image, String timeStamp) {
        this.authorId = authorId;
        this.blogId = blogId;
        this.title = title;
        this.description = description;
        this.image = image;
        this.timeStamp = timeStamp;
    }

    // Getters and Setters
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getBlogId() { return blogId; }
    public void setBlogId(String blogId) { this.blogId = blogId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getTimeStamp() { return timeStamp; }
    public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }
}