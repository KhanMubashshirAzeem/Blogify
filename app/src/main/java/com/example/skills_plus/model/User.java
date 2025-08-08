package com.example.skills_plus.model;

public class User {
    private String uid;
    private String username;
    private String useremail;
    private String profilePhoto;

    public User() {
        // Default constructor for Firebase
    }

    public User(String uid, String username, String useremail, String profilePhoto) {
        this.uid = uid;
        this.username = username;
        this.useremail = useremail;
        this.profilePhoto = profilePhoto;
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUseremail() { return useremail; }
    public void setUseremail(String useremail) { this.useremail = useremail; }
    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
}
