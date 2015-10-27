import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class MonthlyArticles implements InteractableItem, Comparable<MonthlyArticles> {
    @PrimaryKey
    private String dateKey;

    private LocalDate date;

    private Set<Article> articles = new TreeSet<>(Collections.reverseOrder());

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
}
