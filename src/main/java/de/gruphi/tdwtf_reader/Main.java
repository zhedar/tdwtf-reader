package de.gruphi.tdwtf_reader;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sleepycat.je.DatabaseException;
import com.sun.glass.ui.Screen;

import de.gruphi.tdwtf_reader.concurrency.ScrapeMonthlyArticlesTask;
import de.gruphi.tdwtf_reader.db.DataStore;
import de.gruphi.tdwtf_reader.entities.Article;
import de.gruphi.tdwtf_reader.entities.InteractableItem;
import de.gruphi.tdwtf_reader.entities.MonthlyArticles;
import de.gruphi.tdwtf_reader.view.Browser;
import de.gruphi.tdwtf_reader.view.TreeItemRootNode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class Main extends Application {
    private Scene scene;
    private ProgressBar pb;

    private ExecutorService executor;

    private DoubleProperty retrievedMonthCount = new SimpleDoubleProperty(0);
    private static int maxMonthCount = calcNumberOfAvailableMonths();

    @Override
    public void start(Stage stage) throws DatabaseException, Exception {
        stage.setTitle("TDWTF Reader");

        BorderPane pane = new BorderPane();
        TreeItem<InteractableItem> root = new TreeItem<InteractableItem>(
                new TreeItemRootNode("Available Months"));
        root.setExpanded(true);

        executor = Executors.newFixedThreadPool(10);
        iterateMonths(root);

        Browser browser = new Browser();
        TreeView<InteractableItem> tree = new TreeView<>(root);
        tree.setCellFactory(new Callback<TreeView<InteractableItem>, TreeCell<InteractableItem>>() {
            @Override
            public TreeCell<InteractableItem> call(TreeView<InteractableItem> param) {
                return new CheckBoxTreeCell<InteractableItem>() {
                    @Override
                    public void updateItem(InteractableItem item, boolean empty) {
                        super.updateItem(item, empty);
                        // If there is no information for the Cell, make it empty
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                            // Otherwise if it's not representation as an item
                            // of the tree is not a CheckBoxTreeItem, remove the checkbox item
                        } else if (!(getTreeItem() instanceof CheckBoxTreeItem))
                            setGraphic(null);
                        else if(item!=null && !item.provideStyle().isEmpty())
                            setStyle(item.provideStyle());
                    }
                };
            }
        });

        tree.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handle(newValue, browser));

        VBox vb = new VBox();
        vb.setSpacing(2);
        pb = new ProgressBar();
        pb.prefWidthProperty().bind(vb.widthProperty());
        pb.progressProperty().bind(retrievedMonthCount.divide(maxMonthCount));

        vb.getChildren().addAll(pb, tree);
        pane.setLeft(vb);
        pane.setCenter(browser);


        Screen screen = Screen.getMainScreen();

        scene = new Scene(pane, screen.getWidth(), screen.getHeight(), Color.WHITE);
        stage.setScene(scene);

        tree.setPrefHeight(screen.getHeight());

        scene.getStylesheets().add("reader.css");

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                event.consume();

                //cancel workers, which weren't executed yet
                executor.shutdown();

                Platform.exit();
                System.exit(0);
            }
        });
        stage.show();
    }

    private static int calcNumberOfAvailableMonths() {
        LocalDate now = LocalDate.now();
        int count = 0;
        for (int articleYear = 2004; articleYear <= now.getYear(); articleYear++)
            for (int articleMonth = 1; articleMonth <= 12; articleMonth++)
                if (!(articleYear == 2004 && articleMonth < 5) &&
                    !(articleYear == now.getYear() && articleMonth >= now.getMonthValue()))
                    count++;

        return count;
    }

    private void iterateMonths(TreeItem<InteractableItem> root) throws DatabaseException, Exception {
            LocalDate now = LocalDate.now();

            try(DataStore ds = new DataStore()) {
            for (int articleYear = now.getYear(); articleYear >= 2004; articleYear--)
                for (int articleMonth = 12; articleMonth >= 1; articleMonth--) {
                    //dont try to scrape future months
                    if (articleYear == now.getYear() && articleMonth > now.getMonthValue())
                        continue;
                    //first articles are from May 2004, so skip the months before that
                    if (articleYear == 2004 && articleMonth < 5)
                        break;

                    MonthlyArticles ma = ds.getArticles(articleYear, articleMonth);

                    //display already saved months
                    if (ma != null)
                        createTreeItem(root, ma);

                    //scrape articles, if needed
                    //TODO update old months if not fetched yet
                    if (ma == null || (articleYear == now.getYear() && articleMonth == now.getMonthValue())) {
                        Task<MonthlyArticles> task = new ScrapeMonthlyArticlesTask(articleYear, articleMonth);
                        task.setOnSucceeded(event -> createTreeItem(root, (MonthlyArticles) event.getSource().getValue()));
                        task.setOnFailed(event -> System.err.println(task.getException()));
                        executor.execute(task);
                    }
                }
            }
    }

    private void createTreeItem(TreeItem<InteractableItem> root, MonthlyArticles mo) {
        //there's no content to display for this month (yet)
        if(mo == null)
            return;

        TreeItem<InteractableItem> treeItem = new TreeItem<InteractableItem>(mo);

        //in case of an update, remove the older entry
        for(TreeItem<InteractableItem> i : root.getChildren())
            if(i.getValue().toString().equals(mo.toString())) {
                if(root.getValue().equals(mo))
                    root.getChildren().remove(i);
                else
                   return;
                break;
            }

        for (Article a : mo.getArticles()) {
            CheckBoxTreeItem<InteractableItem> cb = new CheckBoxTreeItem<InteractableItem>(a);

            cb.selectedProperty().set(a.isRead());
            cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
                        Boolean newValue) {
                    a.setRead(newValue);
                    try (DataStore d = new DataStore()) {
                        d.updateArticle(a);
                    } catch (DatabaseException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            treeItem.getChildren().add(cb);
        }

        root.setValue(new TreeItemRootNode("Avaiable Months (" + root.getChildren().size() + " / " + maxMonthCount + ")"));
        root.getChildren().add(treeItem);
        root.getChildren().sort((o1, o2) -> ((MonthlyArticles)o2.getValue()).compareTo((MonthlyArticles)o1.getValue()));
        retrievedMonthCount.set(retrievedMonthCount.get()+1);
    }

    private Object handle(TreeItem<InteractableItem> newValue, Browser browser) {
        if (newValue.getValue().getClass().equals(Article.class)) {
            Article a = (Article) newValue.getValue();
            browser.loadArticle(a, ((CheckBoxTreeItem<InteractableItem>) newValue).selectedProperty());
        }

        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}