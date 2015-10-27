import java.time.LocalDate;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PersistentProxy;

@Persistent(proxyFor=LocalDate.class)
public class LocalDateProxy implements PersistentProxy<LocalDate> {

    private int dayOfMonth,
                month,
                year;

    private LocalDateProxy() {}

    public void initializeProxy(LocalDate date) {
        dayOfMonth = date.getDayOfMonth();
        month = date.getMonthValue();
        year = date.getYear();
      }

      public LocalDate convertProxy() {
          return LocalDate.of(year, month, dayOfMonth);
    }
}
