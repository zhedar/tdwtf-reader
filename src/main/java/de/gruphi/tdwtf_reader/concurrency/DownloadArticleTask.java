package de.gruphi.tdwtf_reader.concurrency;

import java.nio.file.Files;
import java.nio.file.Paths;

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
        String articleHtml = articleBody.toString();

        //show mark's legendary html comments.
        articleHtml = articleHtml.replaceAll("<\\!--", "<div style='color:red'>");
        articleHtml = articleHtml.replaceAll("-->", "</div>");

        //embed inline css
        //TODO that's quite ugly
        String css =  String.join("", Files.readAllLines(Paths.get("reader.css")));

        return  "<html><body><style>" + css+ "</style><h1>" +
                article.getTitle() +  "</h1>" + articleHtml + "</html>";
    }

}
