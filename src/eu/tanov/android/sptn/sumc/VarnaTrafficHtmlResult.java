package eu.tanov.android.sptn.sumc;

import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.tanov.android.sptn.LocationView;
import eu.tanov.android.sptn.R;
import eu.tanov.android.sptn.map.StationsOverlay;
import eu.tanov.android.sptn.providers.InitStations;
import eu.tanov.android.sptn.providers.InitStations.PositionVarnaTraffic;

public class VarnaTrafficHtmlResult extends HtmlResult {
    private static final String TAG = "VarnaTrafficHtmlResult";

    private static final String STATION_URL = "http://varnatraffic.com/Ajax/FindStationDevices?stationId=";

    private Response all;

    public VarnaTrafficHtmlResult(LocationView context, StationsOverlay overlay, String stationCode, String stationLabel) {
        super(context, overlay, InitStations.PROVIDER_VARNATRAFFIC, stationCode, stationLabel);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeviceData {
        private int device;
        private int line;
        private String arriveTime;
        private String delay;
        private String arriveIn;
        private String distanceLeft;
        private PositionVarnaTraffic position;

        public int getDevice() {
            return device;
        }

        public void setDevice(int device) {
            this.device = device;
        }

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public String getArriveTime() {
            return arriveTime;
        }

        public void setArriveTime(String arriveTime) {
            this.arriveTime = arriveTime;
        }

        public String getArriveIn() {
            return arriveIn;
        }

        public void setArriveIn(String arriveIn) {
            this.arriveIn = arriveIn;
        }

        public String getDelay() {
            return delay;
        }

        public void setDelay(String delay) {
            this.delay = delay;
        }

        public String getDistanceLeft() {
            return distanceLeft;
        }

        public void setDistanceLeft(String distanceLeft) {
            this.distanceLeft = distanceLeft;
        }

        public PositionVarnaTraffic getPosition() {
            return position;
        }

        public void setPosition(PositionVarnaTraffic position) {
            this.position = position;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @SuppressWarnings("unused")
    private static class ScheduleData {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @SuppressWarnings("unused")
    private static class Response {
        private DeviceData[] liveData;
        private Map<String, ScheduleData[]> schedule;

        public DeviceData[] getLiveData() {
            return liveData;
        }

        public void setLiveData(DeviceData[] liveData) {
            this.liveData = liveData;
        }

        public Map<String, ScheduleData[]> getSchedule() {
            return schedule;
        }

        public void setSchedule(Map<String, ScheduleData[]> schedule) {
            this.schedule = schedule;
        }

    }

    @Override
    public void query() {
        try {

            Log.i(TAG, "fetching: " + STATION_URL + stationCode);

            final URLConnection openConnection = new java.net.URL(STATION_URL + stationCode).openConnection();
            openConnection.setUseCaches(false);
            all = new ObjectMapper().readValue(openConnection.getInputStream(), Response.class);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "could not get estimations (null) for " + stationCode + ". " + stationLabel, e);
        }
        date = new Date();

        htmlData = HTML_START + HTML_HEADER + createBody(all) + HTML_END;
    }

    private String createBody(Response all) {
        final StringBuilder result = new StringBuilder();
        if (all.getLiveData() == null || all.getLiveData().length == 0) {
            result.append(context.getString(R.string.varnatraffic_noData));
        } else {
            result.append("<table border='1'>").append(context.getString(R.string.varnatraffic_estimates_table_header))
                    .append("<tbody>");

            for (DeviceData next : all.getLiveData()) {
                result.append(String
                        .format("<tr><td><a href='http://varnatraffic.com/Line/Index/%s'>%s</a></td><td>%s</td><td>%s</td><td style='white-space: nowrap;'>%s<span class='bus-delay bus-delay-%s'>%s</span></td></tr>",
                                next.getLine(), next.getLine(), next.getArriveIn() == null ? context.getResources()
                                        .getString(R.string.varnatraffic_alreadyLeft) : next.getArriveIn(), next
                                        .getDistanceLeft(), next.getArriveTime(), isGreenDelay(next) ? "green" : "red",
                                next.getDelay() == null ? "" : next.getDelay()));
            }
            result.append("</tbody></table>");
        }
        return result.toString() + context.getString(R.string.legal_varnatraffic_html) + createSchedule(all)
                + context.getString(R.string.legal_varnatraffic_html);
    }

    private String createSchedule(Response all) {
        final StringBuilder result = new StringBuilder();
        if (all.getSchedule() == null || all.getSchedule().size() == 0) {
            result.append(context.getString(R.string.varnatraffic_noData));
        } else {
            result.append("<table border='1'>").append(context.getString(R.string.varnatraffic_schedule_table_header))
                    .append("<tbody>");

            for (Entry<String, ScheduleData[]> next : all.getSchedule().entrySet()) {
                result.append(String.format(
                        "<tr><td><a href='http://varnatraffic.com/Line/Index/%s'>%s</a></td><td>%s</td></tr>",
                        next.getKey(), next.getKey(), getSchedule(next.getValue())));
            }
            result.append("</tbody></table>");
        }
        return result.toString();
    }

    private String getSchedule(ScheduleData[] value) {
        final StringBuilder result = new StringBuilder();

        for (ScheduleData scheduleData : value) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(scheduleData.getText());
        }
        return result.toString();
    }

    private boolean isGreenDelay(DeviceData data) {
        if (data.getDelay() != null && data.getDelay().startsWith("-")) {
            return true;
        }
        return false;
    }

    @Override
    public void showResult(boolean onlyBuses) {
        super.showResult(onlyBuses);
        if (all != null) {
            context.getBusesOverlay().showBusses(Arrays.asList(all.getLiveData()));
        } else {
            context.getBusesOverlay().showBusses(Collections.<DeviceData> emptyList());
        }
    }

    @Override
    public boolean hasBusSupport() {
        return true;
    }
}
