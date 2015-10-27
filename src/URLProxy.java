import java.net.MalformedURLException;
import java.net.URL;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PersistentProxy;

@Persistent(proxyFor = URL.class)
public class URLProxy implements PersistentProxy<URL> {

    private String urlString;

    private URLProxy() {
    }

    public void initializeProxy(URL url) {
        urlString = url.toString();
    }

    public URL convertProxy() {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
