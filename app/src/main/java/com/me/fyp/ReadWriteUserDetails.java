package com.me.fyp;

public class ReadWriteUserDetails
{
    private String username, email, DOB, gender, mobile, country, state;
    private String profile; // Store the URL of the user's profile picture

    // Default constructor (required by Firebase)
    public ReadWriteUserDetails() {}

    // Constructor to initialize all fields including the profile picture URL
    public ReadWriteUserDetails(String email, String username, String DOB, String gender, String mobile, String country, String state) {
        this.email = email;
        this.username = username;
        this.profile = "https://firebasestorage.googleapis.com/v0/b/cancer-survival-3b7f3.appspot.com/o/no_pic_profile.png?alt=media&token=04f681c7-07b4-46ad-884c-15133a41fda5";
        // Set a default profile picture URL in case it's not provided
        this.DOB = DOB;
        this.gender = gender;
        this.mobile = mobile;
        this.country = country;
        this.state = state;
    }

    // Getters and setters for all fields

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getDOB() {
        return DOB;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
