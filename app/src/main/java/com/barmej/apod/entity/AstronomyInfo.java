package com.barmej.apod.entity;

import java.net.URL;

public class AstronomyInfo {
    private String date;
    private String explanation;
    private URL hdUrl;
    private String mediaType;
    private String title;
    private URL url;

    public AstronomyInfo() {
    }

    public AstronomyInfo(String date, String explanation, URL hdUrl, String media_type, String title, URL url) {
        this.date = date;
        this.explanation = explanation;
        this.hdUrl = hdUrl;
        this.mediaType = media_type;
        this.title = title;
        this.url = url;
    }

    public URL getHdUrl() {
        return hdUrl;
    }

    public void setHdUrl(URL hdUrl) {
        this.hdUrl = hdUrl;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }


    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}
