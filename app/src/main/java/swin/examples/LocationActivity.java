
package swin.examples;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/** Displays the current location, including suburb -- does NOT work in emulator */
public class LocationActivity extends Activity
{
    LocationManager locationManager;
    TextView locationView;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initializeUI();
    }

    /** Define a listener that responds to location updates */
    LocationListener locationListener = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            updateLocationOnUI(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            if (status == LocationProvider.AVAILABLE) 
            {
                updateLocationOnUI();
            }
        }

        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
    };

    /** Register the location listener on the location manager */
    private void initializeUI()
    {
        locationView = (TextView) findViewById(R.id.locationTextView);

        // Register listener with Location Manager to receive location updates
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                                                0, 0, locationListener);
    }

    /** Update to show last known location */
    public void getLocationButtonHandler(View v)
    {
        updateLocationOnUI();
    }
    
    /** Show the suburb information */
    public void getSuburbButtonHandler(View v)
    {
        Location loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        updateLocationOnUI(loc);
        TextView suburb = (TextView) findViewById(R.id.suburbTextView);
        suburb.setText("Obtaining data ....");
        new ReverseGeoCoderAsync().execute(loc);
    }

    /** Get last known location and then display it */
    private void updateLocationOnUI()
    {
        Location l = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        updateLocationOnUI(l);
    }

    /** Display date and location data */
    private void updateLocationOnUI(Location location)
    {
        if (location != null)
        {
            String locNow = (new Date()) + ", " + location.toString();
            locationView.setText(locNow);
        }
    }
    
    @Override
    protected void onStop()
    {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }
    
    /** Obtains suburb using reverse geolocation lookup -- requires internet */
    private class ReverseGeoCoderAsync extends AsyncTask<Location, Void, String>
    {
        String locality = "** Suburb Unknown **";  // default

        @Override
        /** Obtain the suburb name using reverse geo location look up */
        // Three dots - a variable length list of arguments - more than one location
        protected String doInBackground(Location... params)
        {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            Location loc = params[0];
            List<Address> addresses = null;
            try
            {
                // Call the synchronous getFromLocation()
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException e) {} // ignore -- will show default
            
            // If we have some results, then extract locality
            if (addresses != null && addresses.size() > 0)
            {
                Address address = addresses.get(0);
                locality = address.getLocality();
            }
            return locality;
        }
        
        @Override
        /** Show the result on the UI */
        protected void onPostExecute(String result)
        {
            TextView suburb = (TextView) findViewById(R.id.suburbTextView);
            suburb.setText(result);
        }
    }    
    
}
