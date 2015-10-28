package de.gruphi.tdwtf_reader;
import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;

import de.gruphi.tdwtf_reader.db.DataStore;
import de.gruphi.tdwtf_reader.entities.Article;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

public class Browser extends Region {

    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();

    private Article currentArticle;
    private BooleanProperty articleReadProp;

    public Browser() throws IOException {
//        getStyleClass().add("browser");

        webEngine.getLoadWorker().stateProperty().addListener((ChangeListener<State>) (ov, oldState, newState) -> {
            if (newState == State.SUCCEEDED) {
                webEngine.executeScript(
                        "a = setInterval(function() {var y = (window.pageYOffset !== undefined) ? window.pageYOffset : (document.documentElement || document.body.parentNode || document.body).scrollTop;y -= 0;var maxY = document.body.scrollHeight -  window.innerHeight;if (y == maxY){alert(a);window.clearInterval(a);}}, 300);");
            }
        });
        webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent<String> e) {
                currentArticle.setRead(true);
                try (DataStore d = new DataStore()) {
                    d.updateArticle(currentArticle);
                    articleReadProp.set(true);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        });

        String articleBaseUrl = "http://thedailywtf.com/articles/";
        webEngine.load(articleBaseUrl);

        // add the web view to the scene
        getChildren().add(browser);
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height) {
        return 750;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 500;
    }

    public void loadUrl(URL url) {
        webEngine.load(url.toString());
    }

    public void loadArticle(Article a, BooleanProperty prop) {
        try {
            webEngine.loadContent(Jsoup.connect(a.getUrl().toString()).get().select("div.article-body").toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // loadUrl(a.getUrl());

        currentArticle = a;
        articleReadProp = prop;
    }
}