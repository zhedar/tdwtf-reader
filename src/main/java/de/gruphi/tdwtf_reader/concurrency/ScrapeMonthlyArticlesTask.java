package de.gruphi.tdwtf_reader.concurrency;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.sleepycat.je.DatabaseException;

import de.gruphi.tdwtf_reader.Constants;
import de.gruphi.tdwtf_reader.db.DBUtil;
import de.gruphi.tdwtf_reader.entities.Article;
import de.gruphi.tdwtf_reader.entities.Author;
import de.gruphi.tdwtf_reader.entities.MonthlyArticles;
import javafx.concurrent.Task;

public class ScrapeMonthlyArticlesTask extends Task<MonthlyArticles> {
    private int year, month;

    public ScrapeMonthlyArticlesTask(int year, int month) {
        this.year = year;
        this.month = month;
    }

    @Override
    protected MonthlyArticles call() throws Exception {
        scrapeMonth(year, month);
        return DBUtil.getArticles(year, month);
    }

    public void scrapeMonth(int articleYear, int articleMonth) throws IOException, DatabaseException {
        String url = Constants.ARTICLE_BASE_URL + articleYear + "/" + articleMonth;
        Document doc = Jsoup.connect(url).timeout(30 * 1000).get();

        for (Element completeArticle : doc.getElementsByClass("articleListItem")) {
            Article article = new Article();
            for (Element articleElement : completeArticle.getAllElements()) {
                switch (articleElement.tagName()) {
                case "header":
                    scrapeMetaInfo(article, articleElement);
                    break;
                case "div":
                    scrapeAuthor(article, articleElement);
                    break;
                case "a":
                    if (articleElement.className().equals("readMore"))
                        try {
                            article.setUrl(new URL(Constants.BASE_URL + articleElement.attr("href")));
                        } catch (MalformedURLException e) {
                            // invalidate article with malformed URL
                            Logger.getGlobal().log(Level.WARNING,
                                    "Encountered malformed URL on " + articleMonth + "/" + articleYear, e);
                            continue;
                        }
                    break;
                }
            }

            DBUtil.insertArticle(article);
        }
    }

    private void scrapeMetaInfo(Article article, Element e2) {
        for (Element headerElement : e2.getAllElements()) {
            if (headerElement.tagName().equals("h4"))
                article.setTitle(headerElement.text());
            else if (headerElement.tagName().equals("h5")) {
                String h5Text = headerElement.text();

                article.setCategory(h5Text.substring(h5Text.indexOf(" in") + 4, h5Text.indexOf(" on")));
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String dateString = h5Text.substring(h5Text.indexOf(" on") + 4, h5Text.length());
                article.setPublished(LocalDate.parse(dateString, df));
            }
        }
    }

    private void scrapeAuthor(Article article, Element e2) {
        if (e2.className().equals("author")) {
            Author author = new Author();
            Element authorElement = e2.getElementsByTag("a").get(0);
            author.setUrl(Constants.BASE_URL + authorElement.attr("href"));
            author.setName(authorElement.text());
            article.setAuthor(author);
        }
    }

}
