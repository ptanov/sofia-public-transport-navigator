package eu.tanov.android.bptnotifications.info;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;

public class NotificationInfo {
    public enum DAYS {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
    private List<String> numbers;
    private String provider;
    private String busStop;
    private EnumSet<DAYS> days;
    private Date at;
    public List<String> getNumbers() {
        return numbers;
    }
    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }
    public String getProvider() {
        return provider;
    }
    public void setProvider(String provider) {
        this.provider = provider;
    }
    public EnumSet<DAYS> getDays() {
        return days;
    }
    public void setDays(EnumSet<DAYS> days) {
        this.days = days;
    }
    public Date getAt() {
        return at;
    }
    public void setAt(Date at) {
        this.at = at;
    }
    public String getBusStop() {
        return busStop;
    }
    public void setBusStop(String busStop) {
        this.busStop = busStop;
    }
    

}
