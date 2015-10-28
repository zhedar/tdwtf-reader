package de.gruphi.tdwtf_reader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.gruphi.tdwtf_reader.entities.Article;
import javafx.concurrent.Task;

public class DownloadArticleTask extends Task<String> {
    private Article article;

    public DownloadArticleTask(Article article) {
        this.article = article;
    }

    @Override
    protected String call() throws Exception {
        Document doc = Jsoup.connect(article.getUrl().toString()).timeout(30 * 1000).get();
        Elements articleBody = doc.select("div.article-body");
        return  "<h1>" + article.getTitle() +  "</h1>" + articleBody.toString();
    }

}
