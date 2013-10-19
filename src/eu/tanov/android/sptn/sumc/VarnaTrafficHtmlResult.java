package eu.tanov.android.sptn.sumc;

import java.util.Date;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.tanov.android.sptn.LocationView;
import eu.tanov.android.sptn.map.StationsOverlay;
import eu.tanov.android.sptn.providers.InitStations;

public class VarnaTrafficHtmlResult extends HtmlResult {
    private static final String TAG = "VarnaTrafficHtmlResult";


    private static final String STATION_URL = "http://varnatraffic.com/Ajax/FindStationDevices?stationId=";



    public VarnaTrafficHtmlResult(LocationView context, StationsOverlay overlay, String stationCode, String stationLabel) {
        super(context, overlay, InitStations.PROVIDER_VARNATRAFFIC, stationCode, stationLabel);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @SuppressWarnings("unused")
    private static class DeviceData {
        private int device;
        private int line;
        private String arriveTime;
        private String arriveIn;

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

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @SuppressWarnings("unused")
    private static class Response {
        private DeviceData[] liveData;

        public DeviceData[] getLiveData() {
            return liveData;
        }

        public void setLiveData(DeviceData[] liveData) {
            this.liveData = liveData;
        }

    }

    @Override
    public void query() {
        final Response all;
        try {
            
            Log.i(TAG, "fetching: " + STATION_URL + stationCode);

            all = new ObjectMapper().readValue(new java.net.URL(
                    STATION_URL + stationCode).openConnection()
                    .getInputStream(), Response.class);
        } catch (Exception e) {
            throw new IllegalStateException("could not get estimations (null) for " + stationCode + ". " + stationLabel, e);
        }
        date = new Date();

        htmlData = HTML_START + HTML_HEADER + createBody(all) + HTML_END;
    }

    private String createBody(Response all) {
        final StringBuilder result = new StringBuilder();;
        for (DeviceData next : all.getLiveData()) {
            result.append(next.line+ ":"+next.arriveIn+"<br />");
        }
        return result.toString();
    }


}
