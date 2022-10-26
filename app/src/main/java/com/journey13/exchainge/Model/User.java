package com.journey13.exchainge.Model;

public class User {

    private String id;
    private String username;
    private String imageURL;
    private String status;
    private String search;
    private String firstName;
    private String secondName;
    private String tagline;
    private Boolean searchable;
    private Boolean searchableByEmail;
    private Boolean searchableByUsername;


    public User(String id, String username, String tagline, String imageURL, String status, String search, String firstName, String secondName, Boolean searchable, Boolean searchableByEmail, Boolean searchableByUsername) {
        this.id = id;
        this.username = username;
        this.tagline = tagline;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
        this.firstName = firstName;
        this.secondName = secondName;
        this.searchable = searchable;
        this.searchableByEmail = searchableByEmail;
        this.searchableByUsername = searchableByUsername;

    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public Boolean getSearchable() {
        return searchable;
    }

    public void setSearchable(Boolean searchable) {
        this.searchable = searchable;
    }

    public Boolean getSearchableByEmail() {
        return searchableByEmail;
    }

    public void setSearchableByEmail(Boolean searchableByEmail) {
        this.searchableByEmail = searchableByEmail;
    }

    public Boolean getSearchableByUsername() {
        return searchableByUsername;
    }

    public void setSearchableByUsername(Boolean searchableByUsername) {
        this.searchableByUsername = searchableByUsername;
    }
}
