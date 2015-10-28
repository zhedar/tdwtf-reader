package de.gruphi.tdwtf_reader;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sleepycat.je.DatabaseException;

import de.gruphi.tdwtf_reader.db.DataStore;
import de.gruphi.tdwtf_reader.entities.Article;
import de.gruphi.tdwtf_reader.entities.InteractableItem;
import de.gruphi.tdwtf_reader.entities.MonthlyArticles;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class Main extends Application {
    private Scene scene;

    private ExecutorService executor;

    static private int maxMonthCount = calcNumberOfAvailableMonths();

    @Override
    public void start(Stage stage) throws DatabaseException, Exception {
        stage.setTitle("TDWTF Reader");

        BorderPane pane = new BorderPane();
        TreeItem<InteractableItem> root = new TreeItem<InteractableItem>(
                new TreeItemRootNode("Avaiable Months"));
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
                        // If there is no information for the Cell, make it
                        // empty
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                            // Otherwise if it's not representation as an item
                            // of the tree
                            // is not a CheckBoxTreeItem, remove the checkbox
                            // item
                        } else if (!(getTreeItem() instanceof CheckBoxTreeItem)) {
                            setGraphic(null);
                        }
                    }
                };
            }
        });

        tree.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handle(newValue, browser));

        pane.setLeft(tree);
        pane.setCenter(browser);
        scene = new Scene(pane, 750, 500, Color.web("#666970"));
        stage.setScene(scene);
        scene.getStylesheets().add("reader.css");

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                event.consume();
                Platform.exit();
                System.exit(0);
                //cancel workers, which weren't executed yet
                executor.shutdown();
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

                    //scrape articles, if needed
                    if (ma == null || (articleYear == now.getYear() && articleMonth == now.getMonthValue()) ) {
                        Task<MonthlyArticles> task = new ScrapeMonthlyArticlesTask(articleYear, articleMonth);
                        task.setOnSucceeded(event -> createTreeItem(root, (MonthlyArticles) event.getSource().getValue()));
                        executor.execute(task);
                    }
                    else
                        createTreeItem(root, ma);

                }
            }
    }

    private void createTreeItem(TreeItem<InteractableItem> root, MonthlyArticles mo) {
        TreeItem<InteractableItem> treeItem = new TreeItem<InteractableItem>(mo);

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