package au.id.tedp.mapdroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.String;
import java.net.URL;
import org.apache.http.impl.client.DefaultHttpClient;

public class Picker extends Activity
{
    private ArrayAdapter<RoutePoint> raa;

    private void setMapLocation(float latitude, float longitude) {
        MapView map = (MapView) findViewById(R.id.mapView);
        map.setCenterCoords(latitude, longitude);
    }

    public static final String SAVED_LATITUDE_KEY = "map_center_latitude";
    public static final String SAVED_LONGITUDE_KEY = "map_center_longitude";
    public static final String SAVED_ZOOM_KEY = "map_zoom";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        setContentView(R.layout.main);

        /*
        final Button btnFindRoute = (Button) findViewById(R.id.btnFindRoute);
        btnFindRoute.setOnClickListener(new FindRouteButtonHandler());
        */

        // XXX: Use the location from the saved state
        // Add a button to select the current location rather than
        // setting it to the current location every time.
        //prefill_fields((Context)this);

        float newLat, newLong;
        int newZoom;

        SharedPreferences settings = getPreferences(MODE_PRIVATE);

        // Default location: The Atlantic Ocean, somewhere between the
        // United States and Europe
        newLat = (float)25.0;
        newLong = (float)-25.0;
        newZoom = 2;

        if (savedState != null) {
            newLat = savedState.getFloat(SAVED_LATITUDE_KEY, newLat);
            newLong = savedState.getFloat(SAVED_LONGITUDE_KEY, newLong);
            newZoom = savedState.getInt(SAVED_ZOOM_KEY, newZoom);
        } else {
            newLat = settings.getFloat(SAVED_LATITUDE_KEY, newLat);
            newLong = settings.getFloat(SAVED_LONGITUDE_KEY, newLong);
            newZoom = settings.getInt(SAVED_ZOOM_KEY, newZoom);
        }

        MapView map = (MapView) findViewById(R.id.mapView);
        map.setCenterCoords(newLat, newLong);
        //map.setZoom(newZoom);
        //updateLocationFields();

        //Debug.startMethodTracing("mapdroid");
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = settings.edit();

        MapView map = (MapView) findViewById(R.id.mapView);

        ed.putFloat(SAVED_LATITUDE_KEY, map.getLatitude());
        ed.putFloat(SAVED_LONGITUDE_KEY, map.getLongitude());
        ed.commit();
    }

    // XXX: Not sure whether this should be onSaveInstanceState or onPause
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        MapView map = (MapView) findViewById(R.id.mapView);
        if (map == null)
            return;

        outState.putFloat(SAVED_LATITUDE_KEY, map.getLatitude());
        outState.putFloat(SAVED_LONGITUDE_KEY, map.getLongitude());
    }

    @Override
    public void onDestroy() {
        //Debug.stopMethodTracing();
        super.onDestroy();
    }

    // FIXME: Replace this with overlay handling like the Maps app
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        MapView map = (MapView) findViewById(R.id.mapView);
        int change = 0;

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
            change = -1;
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
            change = +1;

        if (change != 0) {
            map.setZoom(map.getZoom() + change);
            return true;
        }

        return false;
    }

    public Location getLastLocation(Context ctx) {
        // XXX: Can this be static final?
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        LocationManager locmgr = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

        String bestProvider = locmgr.getBestProvider(criteria, true);
        if (bestProvider == null)
            return null;

        // XXX: Could be out-of-date or disabled
        return locmgr.getLastKnownLocation(bestProvider);
    }

    // We probably want to require high-res (GPS) location rather than low-res
    // but this will do for now.
    public String getLastLocationAsString(Context ctx) {
        Location loc = getLastLocation(ctx);

        if (loc == null)
            return null;

        return String.format("%f,%f", loc.getLatitude(), loc.getLongitude());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.my_location:
            Location loc = getLastLocation((Context)this);
            // FIXME: Notify user if there is no location source
            // or turn GPS on for them
            if (loc != null) {
                MapView map = (MapView) findViewById(R.id.mapView);
                map.setCenterCoords((float)loc.getLatitude(), (float)loc.getLongitude());
            }
            return true;
        }
        return false;
    }
}

/* vim: set ts=4 sw=4 et ai :*/