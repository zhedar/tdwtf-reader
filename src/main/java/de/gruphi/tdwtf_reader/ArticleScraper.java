package de.gruphi.tdwtf_reader;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.sleepycat.je.DatabaseException;

import de.gruphi.tdwtf_reader.db.DataStore;
import de.gruphi.tdwtf_reader.entities.Article;
import de.gruphi.tdwtf_reader.entities.Author;
import de.gruphi.tdwtf_reader.entities.MonthlyArticles;

public class ArticleScraper {
    static private final String BASE_URL = "http://thedailywtf.com",
                                ARTICLE_BASE_URL = BASE_URL + "/articles/";

    static private DataStore db = null;

    static public List<MonthlyArticles> iterateMonths() throws DatabaseException, Exception {
        try (DataStore d = new DataStore()) {
            db = d;
            long start = System.currentTimeMillis();
            LocalDate now = LocalDate.now();
            List<MonthlyArticles> moArticles = new ArrayList<>();

            for (int articleYear = 2004; articleYear <= now.getYear(); articleYear++)
                for (int articleMonth = 1; articleMonth <= 12; articleMonth++) {
                    //first articles are from May 2004, so skip the months before that
                    if (articleYear == 2004 && articleMonth < 5)
                        continue;
                    //dont try to scrape future months
                    if (articleYear == now.getYear() && articleMonth > now.getMonthValue())
                        break;

                    System.out.println(articleMonth + " " + articleYear);

                    MonthlyArticles ma = db.getArticles(LocalDate.of(articleYear, articleMonth, 1));

                    //scrape articles, if needed
                    if (ma == null || articleMonth == now.getMonthValue()) {
//                        ma = new ScrapeMonthlyArticlesTask(db, articleYear, articleMonth);
                        System.out.println("null");
                    }

                    if (ma != null) {
                        System.out.println("NOT NULL");
                        moArticles.add(ma);
                    }
                    else
                        System.err.println("null!!!");
                    // scrape(articleYear, articleMonth);
                }

            Collections.sort(moArticles, Collections.reverseOrder());

            return moArticles;
        }
    }

    static public void scrapeMonth(int articleYear, int articleMonth) throws IOException, ParseException {
        Document doc2 = Jsoup.connect(ARTICLE_BASE_URL + articleYear + "/" + articleMonth).timeout(30 * 1000).get();
        for (Element e : doc2.getElementsByClass("articleListItem")) {
            Article article = new Article();
            for (Element e2 : e.getAllElements()) {
                switch (e2.tagName()) {
                case "header":
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
                    break;
                case "div":
                    if (e2.className().equals("author")) {
                        Author author = new Author();
                        Element authorElement = e2.getElementsByTag("a").get(0);
                        author.setUrl(BASE_URL + authorElement.attr("href"));
                        author.setName(authorElement.text());
                        article.setAuthor(author);
                    }
                    // if(e2.className().equals("container") &&
                    // article.getPreviewText() == null)
                    // article.setPreviewText(e2.html());
                    break;
                case "a":
                    if (e2.className().equals("readMore"))
                        article.setUrl(new URL(BASE_URL + e2.attr("href")));
                    break;
                }
            }

            db.insertArticle(article);
        }
    }

    public static void main(MonthlyArticles[] args) throws DatabaseException, Exception {
        try (DataStore d = new DataStore()) {
            db = d;
            iterateMonths();
        }
    }

}
