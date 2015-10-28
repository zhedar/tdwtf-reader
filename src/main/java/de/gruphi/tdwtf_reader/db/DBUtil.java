package de.gruphi.tdwtf_reader.db;

import com.sleepycat.je.DatabaseException;

import de.gruphi.tdwtf_reader.entities.Article;
import de.gruphi.tdwtf_reader.entities.MonthlyArticles;

public class DBUtil {
    static public MonthlyArticles getArticles(int year, int month) throws DatabaseException {
        try(DataStore ds = new DataStore()) {
            return ds.getArticles(year, month);
        }
    }

    static  public void updateArticle(Article article) throws DatabaseException {
        try(DataStore ds = new DataStore()) {
            ds.updateArticle(article);
        }
    }

    public static void insertArticle(Article article) {
        try(DataStore ds = new DataStore()) {
            ds.insertArticle(article);
        }
    }
}
