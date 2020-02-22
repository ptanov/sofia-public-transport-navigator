package eu.tanov.android.sptn.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import eu.tanov.android.bptcommon.VarnaTrafficHtmlResult;
import eu.tanov.android.bptcommon.interfaces.IBusesOverlay;
import eu.tanov.android.sptn.R;

public class BusesOverlay implements IBusesOverlay {
    private static final int FONT_SIZE = 18;
    private static final int TITLE_MARGIN = 2;

    private GoogleMap map;
    private List<Marker> markers = Collections.emptyList();
    private final int markerHeight;
    private final Context context;

    public BusesOverlay(Context context, GoogleMap map) {
//        super(boundCenterBottom(context.getResources().getDrawable(R.drawable.bus)));
        this.context = context;
        markerHeight = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.bus)).getBitmap().getHeight();

        this.map = map;

//        populateFixed();
    }

    public void showBusses(List<VarnaTrafficHtmlResult.DeviceData> buses) {
        for (Marker next: markers) {
            next.remove();
        }
        this.markers = new ArrayList<Marker>(buses.size());
        for (VarnaTrafficHtmlResult.DeviceData next : buses) {
            if (next.getPosition() != null) {
                final LatLng latLng = new LatLng(next.getPosition().getLat(), next.getPosition().getLon());

                Marker marker = map.addMarker(new MarkerOptions().position(latLng).title(String.format("%s (%s, %s)", next.getLine(),
                        next.getArriveIn() == null ? context.getResources()
                                .getString(R.string.varnatraffic_alreadyLeft) : next.getArriveIn(), next
                                .getDistanceLeft())).snippet(next.getArriveIn()).icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.bus)));
                marker.showInfoWindow();
                markers.add(marker);
            }
        }
    }

    /**
     * from http://groups.google.com/group/android-developers/browse_thread/thread/38b11314e34714c3
     * http://developmentality
     * .wordpress.com/2009/10/19/android-itemizedoverlay-arrayindexoutofboundsexception-nullpointerexception-
     * workarounds/
     */
//    private void populateFixed() {
//        setLastFocusedIndex(-1);
//        populate();
        // redraw items
//        map.invalidate();
//    }

//    @Override
//    protected OverlayItem createItem(int i) {
//        return items.get(i);
//    }
//
//    @Override
//    public int size() {
//        return items.size();
//    }

    /*
     * based on http://binwaheed.blogspot.com/2011/05/android-display-title-on-marker-in.html
     */
//    @Override
//    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
////        super.draw(canvas, mapView, false);
//
//        // go through all OverlayItems and draw title for each of them
//        for (OverlayItem item : items) {
//            /*
//             * Converts latitude & longitude of this overlay item to coordinates on screen. As we have called
//             * boundCenterBottom() in constructor, so these coordinates will be of the bottom center position of the
//             * displayed marker.
//             */
//            GeoPoint point = item.getPoint();
//            Point markerBottomCenterCoords = new Point();
//            mapView.getProjection().toPixels(point, markerBottomCenterCoords);
//
//            /* Find the width and height of the title */
//            TextPaint paintText = new TextPaint();
//            Paint paintRect = new Paint();
//
//            Rect rect = new Rect();
//            paintText.setTextSize(FONT_SIZE);
//            paintText.getTextBounds(item.getTitle(), 0, item.getTitle().length(), rect);
//
//            rect.inset(-TITLE_MARGIN, -TITLE_MARGIN);
//            rect.offsetTo(markerBottomCenterCoords.x - rect.width() / 2, markerBottomCenterCoords.y - markerHeight
//                    - rect.height());
//
//            paintText.setTextAlign(Paint.Align.CENTER);
//            paintText.setTextSize(FONT_SIZE);
//            paintText.setARGB(255, 255, 255, 255);
//            paintRect.setARGB(130, 0, 0, 0);
//
//            canvas.drawRoundRect(new RectF(rect), 2, 2, paintRect);
//            canvas.drawText(item.getTitle(), rect.left + rect.width() / 2, rect.bottom - TITLE_MARGIN, paintText);
//        }
//    }

}
