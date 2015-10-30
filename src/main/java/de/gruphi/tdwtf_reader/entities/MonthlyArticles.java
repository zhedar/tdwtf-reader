package de.gruphi.tdwtf_reader.entities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.TreeSet;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class MonthlyArticles implements InteractableItem, Comparable<MonthlyArticles> {
    @PrimaryKey
    private String dateKey;

    private LocalDate date;

    private Set<Article> articles = new TreeSet<>();

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
        this.dateKey = date.toString();
    }

    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }

    public String getDateKey() {
        return dateKey;
    }

    @Override
    public String toString() {
        return date.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
    }

    @Override
    public int compareTo(MonthlyArticles o) {
        return getDate().compareTo(o.getDate());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((articles == null) ? 0 : articles.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((dateKey == null) ? 0 : dateKey.hashCode());
        return result;
    }

    /* (non-Javadoc)
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
        MonthlyArticles other = (MonthlyArticles) obj;
        if (articles == null) {
            if (other.articles != null)
                return false;
        } else if (!articles.equals(other.articles))
            return false;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (dateKey == null) {
            if (other.dateKey != null)
                return false;
        } else if (!dateKey.equals(other.dateKey))
            return false;
        return true;
    }
}
