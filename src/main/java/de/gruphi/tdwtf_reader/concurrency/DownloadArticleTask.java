package de.gruphi.tdwtf_reader.concurrency;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.gruphi.tdwtf_reader.entities.Article;
import javafx.concurrent.Task;

/**
 * An asynchronous {@link Task}, which does a GET request
 * on an DWTF article url and extracts the HTML content.<br/>
 * Also does some post-processing on the original HTML.
 */
public class DownloadArticleTask extends Task<String> {
    private Article article;

    public DownloadArticleTask(Article article) {
        this.article = article;
    }

    @Override
    protected String call() throws Exception {
        Document doc = Jsoup.connect(article.getUrl().toString()).timeout(30 * 1000).get();
        Element articleBody = doc.select("div.article-body").first();

        if(articleBody == null)
            return "<h1>Error<h1/><h2>No article body found.</h2>";

        for (Element articleElement : articleBody.getAllElements())
        {
            //TODO implement comment substitution this way, if needed
//            for(Node n: articleElement.childNodes())
//                if(n instanceof Comment) {
//                    System.out.println(((Comment) n).getData());
//                }

            //remove ad from botton for better article completion detection
            if(articleElement.text().startsWith("[Advertisement]"))
                articleElement.remove(); //TODO show this ad somewhere else, we need to be polite.
        }
        String articleHtml = articleBody.toString();

        //show html comments as made by remy.
        //TODO test if this affects html and xml comments in shown code
        articleHtml = articleHtml.replaceAll("<\\!--", "<div style='color:red'>");
        articleHtml = articleHtml.replaceAll("-->", "</div>");

        //embed inline css
        //TODO that's quite ugly, externalize stylesheets
        String css =  String.join("", Files.readAllLines(Paths.get("src/main/resources/reader.css")));

        return  "<html><body><style>" + css+ "</style><h1>" +
                article.getTitle() +  "</h1>" + articleHtml + "</html>";
    }

}
