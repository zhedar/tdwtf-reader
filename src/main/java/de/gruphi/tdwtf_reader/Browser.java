package de.gruphi.tdwtf_reader;
import java.io.IOException;
import java.net.URL;

import com.sun.glass.ui.Screen;

import de.gruphi.tdwtf_reader.concurrency.DownloadArticleTask;
import de.gruphi.tdwtf_reader.db.DataStore;
import de.gruphi.tdwtf_reader.entities.Article;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

public class Browser extends Region {
    private final WebView browser = new WebView();
    private final WebEngine webEngine = browser.getEngine();
    private final ProgressBar progress = new ProgressBar();
    private final VBox vb;

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

        vb = new VBox();
        vb.setSpacing(2);
        vb.setAlignment(Pos.CENTER);
        progress.prefWidthProperty().bind(vb.widthProperty());
        progress.visibleProperty().set(Boolean.FALSE);
        vb.getChildren().addAll(progress, browser);

        browser.setPrefHeight(Screen.getMainScreen().getHeight());
        getChildren().add(vb);
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(vb, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    public void loadUrl(URL url) {
        webEngine.load(url.toString());
    }

    public void loadArticle(Article a, BooleanProperty prop) {
        Task<String> task = new DownloadArticleTask(a);
        task.setOnRunning(e -> progress.visibleProperty().setValue(Boolean.TRUE));
        task.setOnSucceeded(event -> onFinishLoading(event));
        task.setOnFailed(e -> progress.visibleProperty().setValue(Boolean.FALSE));
        new Thread(task).start();

        currentArticle = a;
        articleReadProp = prop;
    }

    public void onFinishLoading(WorkerStateEvent event) {
        webEngine.loadContent(event.getSource().getValue().toString());
        progress.visibleProperty().setValue(Boolean.FALSE);
    }
}