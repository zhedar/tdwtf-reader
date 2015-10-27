import com.sleepycat.persist.model.Persistent;

@Persistent
public class Author {
    private String name,
                   url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Author [name=" + name + ", url=" + url + "]";
    }

}
