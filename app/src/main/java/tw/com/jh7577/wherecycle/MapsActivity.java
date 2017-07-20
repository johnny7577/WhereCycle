package tw.com.jh7577.wherecycle;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.FutureTask;

import tw.com.jh7577.wherecycle.utils.GoogleApiAddress;

public class MapsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LatLng mTarget;
    private Polyline mPolyline;
    private Marker mMarker;
    private boolean isInfoShow = true;
    private boolean isOldLocation = false;
    private Context mContext;
    private SharedPreferences mSaredPref;
    private int mPermissionCheck;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mContext = this;
        mSaredPref = getPreferences(Context.MODE_PRIVATE);
        //
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        setUpMapIfNeeded();

        loadLocation();

        // Assume thisActivity is the current activity
        mPermissionCheck = ContextCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION);


        if (mPermissionCheck != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                marker.hideInfoWindow();
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                mTarget = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                getAddressFromMarker(marker);
                saveLocation(mTarget);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (isInfoShow) {
                    marker.hideInfoWindow();
                    isInfoShow = false;
                    return true;
                } else {
                    isInfoShow = true;
                    return false;
                }
            }
        });

        //merge
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the task you need to do.
                    mPermissionCheck = grantResults[0];
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    finish();
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                UiSettings ui = mMap.getUiSettings();
                ui.setZoomControlsEnabled(true);

                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .build();
            }
        }
    }

    private void getAddressFromMarker(Marker marker){

        GoogleApiAddress ga = new GoogleApiAddress(marker.getPosition());
        FutureTask<String> futureTask = new FutureTask<>(ga);
        new Thread(futureTask).start();

        try {
            String tts = futureTask.get();
            marker.setSnippet(tts);
            marker.showInfoWindow();
        } catch (Exception e) {
            Toast.makeText(mContext, "Not Found!", Toast.LENGTH_SHORT).show();
        }
    }

    public void showMyLocation() {
        if (mGoogleApiClient.isConnected()) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LatLng tts = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tts, 17));
        }
    }

    private void saveLocation(LatLng tts){
        mSaredPref.edit().putString("Lat",  tts.latitude+"")
        .putString("Lng", tts.longitude+"").apply();
    }

    private void loadLocation(){
        String strLat = mSaredPref.getString("Lat", "");
        String strLng = mSaredPref.getString("Lng", "");

        if(!strLat.isEmpty() && !strLng.isEmpty()){
            double lat = Double.parseDouble(strLat);
            double lng = Double.parseDouble(strLng);
            mTarget = new LatLng(lat, lng);
            mMarker = mMap.addMarker(new MarkerOptions()
                    .position(mTarget)
                    .title(getString(R.string.park_location))
                    .draggable(true));
            getAddressFromMarker(mMarker);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mTarget, 17));
            isOldLocation = true;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id){
            case R.id.action_getLocation:
                showMyLocation();
                mMap.clear();
                mTarget = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                mMarker = mMap.addMarker(new MarkerOptions()
                        .position(mTarget)
                        .title(getString(R.string.park_location))
                        .draggable(true));
                getAddressFromMarker(mMarker);

                saveLocation(mTarget);
                break;
            case R.id.action_navigate:

                if(isConnection()){
                    if(mPolyline!=null){
                        mPolyline.remove();
                    }
                    Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    String url;
                    if(mTarget != null){
                        url = getDirectionsUrl(new LatLng(location.getLatitude(), location.getLongitude()),
                                mTarget);
                        DownloadTask downloadTask = new DownloadTask();
                        // Start downloading json data from Google Directions
                        // API
                        try {
                            downloadTask.execute(url);
                        } catch(Exception e) {
                            Toast.makeText(mContext, "direction error...", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(mContext, "no destination...", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(mContext, "Please check your internet...", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(mPermissionCheck != -1){
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    REQUEST,
                    this);  // LocationListener
            if(!isOldLocation){
                showMyLocation();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    private boolean isConnection(){
        ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()){
            //do something
            return true;
        }else{
            //do something
            return false;
        }
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + ","
                + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String mode = "mode=walking";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters;

        System.out.println(url);
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(25);
                lineOptions.color(getResources().getColor(R.color.direction_line));
                lineOptions.geodesic(true);
            }

            // Drawing polyline in the Google Map for the i-th route
            mPolyline = mMap.addPolyline(lineOptions);

        }
    }
}
