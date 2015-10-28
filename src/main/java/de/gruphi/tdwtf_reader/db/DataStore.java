package de.gruphi.tdwtf_reader.db;

import java.io.File;
import java.time.LocalDate;
import java.util.Set;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.model.AnnotationModel;
import com.sleepycat.persist.model.EntityModel;

import de.gruphi.tdwtf_reader.Constants;
import de.gruphi.tdwtf_reader.entities.Article;
import de.gruphi.tdwtf_reader.entities.MonthlyArticles;

public class DataStore implements AutoCloseable {
    private File dbPath = new File("tdwtf-db");
    private Environment env;
    private EntityStore articleStore;

    public DataStore() throws DatabaseException {
        startup();
    }

    private void startup() throws DatabaseException {
//        long start = System.currentTimeMillis();
        EnvironmentConfig envcfg = new EnvironmentConfig();
        envcfg.setAllowCreate(true);
        envcfg.setReadOnly(false);
        env = new Environment(dbPath, envcfg);

        StoreConfig stcfg = new StoreConfig();
        stcfg.setAllowCreate(true);
        stcfg.setReadOnly(false);
        EntityModel model = new AnnotationModel();
        model.registerClass(LocalDateProxy.class);
        model.registerClass(URLProxy.class);
        stcfg.setModel(model);
        articleStore = new EntityStore(env, "articlestore", stcfg);
//        System.out.println("boottime:" + (System.currentTimeMillis() - start));
    }

    public void insertArticle(Article article) throws DatabaseException {
        PrimaryIndex<String, MonthlyArticles> pIndex = articleStore.getPrimaryIndex(String.class,
                MonthlyArticles.class);

        LocalDate monthDate = article.getPublished().withDayOfMonth(1);
        if (!pIndex.contains(monthDate.toString())) {
            Constants.logger.info("Create monthly articles set: " + monthDate);
            MonthlyArticles newEntity = new MonthlyArticles();
            newEntity.setDate(monthDate);
            pIndex.put(newEntity);
        }

        MonthlyArticles entity = pIndex.get(monthDate.toString());
        Set<Article> articles = entity.getArticles();
        articles.add(article);

        pIndex.put(entity);
    }

    public void updateArticle(Article article) throws DatabaseException {
        PrimaryIndex<String, MonthlyArticles> pIndex = articleStore.getPrimaryIndex(String.class,
                MonthlyArticles.class);

        MonthlyArticles entity = pIndex.get(article.getPublished().withDayOfMonth(1).toString());
        Set<Article> articles = entity.getArticles();
        articles.remove(article);
        articles.add(article);

        pIndex.put(entity);
    }

    public MonthlyArticles getArticles(int year, int month) throws DatabaseException {
        return getArticles(LocalDate.of(year, month, 1));
    }

    public MonthlyArticles getArticles(LocalDate ld) throws DatabaseException {
        return articleStore.getPrimaryIndex(String.class, MonthlyArticles.class).get(ld.toString());
    }

    private void shutdown() throws DatabaseException {
        articleStore.close();
        env.close();
    }

    @Override
    public void close(){
        shutdown();
    }
}
