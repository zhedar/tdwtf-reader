package de.gruphi.tdwtf_reader.entities;

import java.net.URL;
import java.time.LocalDate;

import com.sleepycat.persist.model.Persistent;

@Persistent(version = 1)
public class Article implements InteractableItem, Comparable<Article>{
    private Author author;
    private String title, category, previewText;

    private URL url;

    private LocalDate published;

    private boolean read = false;

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPreviewText() {
        return previewText;
    }

    public void setPreviewText(String previewText) {
        this.previewText = previewText;
    }

    public LocalDate getPublished() {
        return published;
    }

    public void setPublished(LocalDate published) {
        this.published = published;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return title;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((author == null) ? 0 : author.hashCode());
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((published == null) ? 0 : published.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Article other = (Article) obj;
        if (author == null) {
            if (other.author != null)
                return false;
        } else if (!author.equals(other.author))
            return false;
        if (category == null) {
            if (other.category != null)
                return false;
        } else if (!category.equals(other.category))
            return false;
        if (published == null) {
            if (other.published != null)
                return false;
        } else if (!published.equals(other.published))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }

    @Override
    public int compareTo(Article o) {
        return o.getPublished().compareTo(getPublished());
    }

    @Override
    public String provideStyle() {
        switch(category) {
            case "CodeSOD":
                return "-fx-color:#c1103b";
            case "Error'd":
                return "-fx-color:#E4A838";
            case "Feature Articles":
                return "-fx-color:#2db8c2";
            case "Tales from the Interview":
                return "-fx-color:#f9622f";
            case "Editor's Soapbox":
                return "-fx-color:#95397a";
            default:
                return "";
        }
    }
}
