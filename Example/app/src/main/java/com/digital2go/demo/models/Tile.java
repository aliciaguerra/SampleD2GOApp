package com.digital2go.demo.models;

/**
 * Created by Ulises on 27/12/2017.
 */

public class Tile {
    private String link;
    private String text;
    private int resource = 0;
    private String img_uri;

    public Tile(String link, String text, int resource, String img_uri) {
        this.link = link;
        this.text = text;
        this.resource = resource;
        this.img_uri = img_uri;
    }

    public String getImg_uri() {
        return img_uri;
    }

    public void setImg_uri(String img_uri) {
        this.img_uri = img_uri;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String url) {
        this.link = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getImg() {
        return resource;
    }

    public void setImg(int img) {
        this.resource = img;
    }
}
