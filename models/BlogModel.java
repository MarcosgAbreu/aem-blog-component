package com.adobe.aem.guides.wknd.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.text.SimpleDateFormat;
import java.util.Date;


@Model(adaptables = Resource.class)
public class BlogModel {

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String subtitle;

    @ValueMapValue
    private String imagePath;

    @ValueMapValue
    private String content;

    @ValueMapValue
    private String author;

    @ValueMapValue
    private Date publicationDate;

    @ValueMapValue
    private String tags;

    public BlogModel() {
    }

    public boolean isValid() {
        return !title.isEmpty() && !subtitle.isEmpty() && !imagePath.isEmpty() && !content.isEmpty() && !author.isEmpty() && !tags.isEmpty();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        try {
            this.publicationDate = sdf.parse(publicationDate);
        } catch (Exception e) {
            throw new RuntimeException("Formato de data inválido. Erro: " + e.getMessage());
        }
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}