package eu.tanov.android.sptn.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextPaint;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import eu.tanov.android.sptn.R;
import eu.tanov.android.sptn.sumc.VarnaTrafficHtmlResult.DeviceData;
import eu.tanov.android.sptn.util.MapHelper;

public class BusesOverlay extends ItemizedOverlay<OverlayItem> {
    private static final int FONT_SIZE = 12;
    private static final int TITLE_MARGIN = 3;

    private MapView map;
    private List<OverlayItem> items = Collections.emptyList();
    private final int markerHeight;

    public BusesOverlay(Context context, MapView map) {
        super(boundCenterBottom(context.getResources().getDrawable(R.drawable.bus)));
        markerHeight = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.bus)).getBitmap().getHeight();

        this.map = map;

        populateFixed();
    }

    public void showBusses(List<DeviceData> buses) {
        this.items = new ArrayList<OverlayItem>(buses.size());
        for (DeviceData next : buses) {
            if (next.getPosition() != null) {
                final GeoPoint point = new GeoPoint(MapHelper.toE6(next.getPosition().getLat()), MapHelper.toE6(next
                        .getPosition().getLon()));
                this.items.add(new OverlayItem(point, String.format("%s (%s, %s)", next.getLine(), next.getArriveIn(), next.getDistanceLeft()),
                        next.getArriveIn()));
            }
        }

        populateFixed();
    }

    /**
     * from http://groups.google.com/group/android-developers/browse_thread/thread/38b11314e34714c3
     * http://developmentality
     * .wordpress.com/2009/10/19/android-itemizedoverlay-arrayindexoutofboundsexception-nullpointerexception-
     * workarounds/
     */
    private void populateFixed() {
        setLastFocusedIndex(-1);
        populate();
        // redraw items
        map.invalidate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return items.get(i);
    }

    @Override
    public int size() {
        return items.size();
    }

    /*
     * based on http://binwaheed.blogspot.com/2011/05/android-display-title-on-marker-in.html
     */
    @Override
    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, false);

        // go through all OverlayItems and draw title for each of them
        for (OverlayItem item : items) {
            /*
             * Converts latitude & longitude of this overlay item to coordinates on screen. As we have called
             * boundCenterBottom() in constructor, so these coordinates will be of the bottom center position of the
             * displayed marker.
             */
            GeoPoint point = item.getPoint();
            Point markerBottomCenterCoords = new Point();
            mapView.getProjection().toPixels(point, markerBottomCenterCoords);

            /* Find the width and height of the title */
            TextPaint paintText = new TextPaint();
            Paint paintRect = new Paint();

            Rect rect = new Rect();
            paintText.setTextSize(FONT_SIZE);
            paintText.getTextBounds(item.getTitle(), 0, item.getTitle().length(), rect);

            rect.inset(-TITLE_MARGIN, -TITLE_MARGIN);
            rect.offsetTo(markerBottomCenterCoords.x - rect.width() / 2, markerBottomCenterCoords.y - markerHeight
                    - rect.height());

            paintText.setTextAlign(Paint.Align.CENTER);
            paintText.setTextSize(FONT_SIZE);
            paintText.setARGB(255, 255, 255, 255);
            paintRect.setARGB(130, 0, 0, 0);

            canvas.drawRoundRect(new RectF(rect), 2, 2, paintRect);
            canvas.drawText(item.getTitle(), rect.left + rect.width() / 2, rect.bottom - TITLE_MARGIN, paintText);
        }
    }

}
