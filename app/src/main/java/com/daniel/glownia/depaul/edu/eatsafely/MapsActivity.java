package com.daniel.glownia.depaul.edu.eatsafely;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMarkerClickListener, GoogleMap.OnCameraChangeListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker myLocMarker;
    AsyncTask transfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
//        int counter = 0;
//        for(Marker s: resMarker){
//            if(s.equals(marker)){
//                s.showInfoWindow();
//                Log.i("MyActivity", "Marker Click");
//                final int counter2 = counter;
//                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//                    @Override
//                    public void onInfoWindowClick(Marker marker) {
//                        Intent intent = new Intent(MapsActivity.this,Details.class);
//                        intent.putExtra("res",myRestaurants.get(counter2));
//                        startActivityForResult(intent,0);
//                    }
//                });
//            }
//            counter++;
//        }
       return true;
    }
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                try{
                    setUpMap();
                }
                catch (Exception e){
                    String x = e.toString();
                    Log.i("MapsActivity CONNECTION ERROR", x);
                }
            }
        }
    }



    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() throws IOException {
        Log.i("MapsActivity", "The map is loading!");

        //mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.setOnCameraChangeListener(this);
        this.setMyMarker();

    }

    private void setMyMarker(){
        mMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria,true);
        Location myLocation = locationManager.getLastKnownLocation(provider);

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        double latitude = myLocation.getLatitude();
        double longitude = myLocation.getLongitude();

        LatLng latLng = new LatLng(latitude,longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        Log.i("MapsActivity", "MarkerPlaced");

        myLocMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    private VisibleRegion getVisibleRegion(){
        final VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
        LatLng farRight = visibleRegion.latLngBounds.southwest;
        LatLng nearLeft = visibleRegion.latLngBounds.northeast;

        Log.i("MapsActivity", "getVisibleRegion called: far right" + farRight.toString() + " nearLeft" + nearLeft.toString());
        return visibleRegion;

    }
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.d("MapsActivity","OnCameraChange");
        VisibleRegion visibleRegion = this.getVisibleRegion();

        Double southwest_lat = visibleRegion.latLngBounds.southwest.latitude;
        Double southwest_long = visibleRegion.latLngBounds.southwest.longitude;
        Double northeast_lat = visibleRegion.latLngBounds.northeast.latitude;
        Double northeast_long = visibleRegion.latLngBounds.northeast.longitude;

        String lat_long = southwest_lat + "," + southwest_long + ";" + northeast_lat + "," + northeast_long;
        Log.d("MapsActivity To String",lat_long);

        //create a new async task with the lag lang coordinates
        new GetRestaurants().execute(lat_long);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        transfer.cancel(true);
    }

    private class GetRestaurants extends AsyncTask<String, Integer, String> implements OnMarkerClickListener{
        private ArrayList<Marker> markers = new ArrayList<Marker>();
        @Override
        protected String doInBackground(String... params) {
            Log.d("MapsActivity doInBackground ", "Method called");

            try{
                int count = params.length;
                String coordinates = params[0];
                String input;
                Socket client_socket;

                Log.d("MapsActivity doInBackground ", "About to connect to socket");
                Log.d("MapsActivity doInBackground ", "" + count + "  " + params[0]);
                client_socket = new Socket("207.181.245.105", 7000);

                //write coordinates
                Log.d("MapsActivity doInBackground ", "About to write data");
                DataOutputStream dataOutput = new DataOutputStream(client_socket.getOutputStream());
                dataOutput.writeUTF("-l");
                dataOutput.writeUTF(coordinates);
                //read coordinates
                Log.d("MapsActivity doInBackground ", "About to read data");
                DataInputStream dataInput = new DataInputStream(client_socket.getInputStream());
                input = dataInput.readUTF();
                client_socket.close();

                Log.d("MapsActivity doInBackground ", "Done with operation");
                Log.d("MapsActivity doInBackground ", input);
                return input;
            }
            catch(Exception e){
                Log.d("MapsActivity doInBackground ",e.toString());
                return "bob";
            }
        }

        @Override
        protected void onPreExecute() {
            Context context = getApplicationContext();
            mMap.clear();
            CharSequence text = "Loading...";
            int duration = Toast.LENGTH_SHORT;
            //markers = null;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            if(markers.size() > 0){
                markers.clear();
                Log.d("MapsActivity onPreExecute: ", "Arraylist Markers has been cleared");
            }
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("MapsActivity onPostExecute ", "result:" + s);
            String spiltBy = ";";
            String splitBy2 = ",";

            mMap.setOnMarkerClickListener(this);

            String[] location = s.split(spiltBy);
            if(location[0].equals("-n")){
                Context context = getApplicationContext();
                CharSequence text = "Zoom In: " + location[1].toString() + " found!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            else{
                for (int i = 1; i < location.length;i++){
                    //Log.d("MapsActivity onPostExecute ", "restaurant location:" + location[i].toString());
                    String[] restaurant = location[i].split(splitBy2);
                    double latitude = Double.parseDouble(restaurant[0]);
                    double longitude = Double.parseDouble(restaurant[1]);
                    //Log.d("MapsActivity onPostExecute ", "restaurant location:" + latitude + " " + longitude);
                    if(restaurant[2].equals("1")){
                        //add green marker
                        myLocMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(restaurant[3].toString()).snippet("PASS: " + restaurant[4].toString()));
                        markers.add(myLocMarker);
                    }
                    else{
                        //add red marker
                        myLocMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(restaurant[3].toString()).snippet("FAIL: " + restaurant[4].toString()));
                        markers.add(myLocMarker);
                    }
                }
            }

        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            Log.d("MapsActivity onMarkerClick ", "markerClicked");
            for(Marker m : markers){
                if (marker.isInfoWindowShown()) {
                    marker.hideInfoWindow();
                } else {
                    marker.showInfoWindow();
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            LatLng x =  marker.getPosition();
                            Log.d("MapsActivity onInfoClickListener ", "info window clicked");
                            String latlong = x.latitude + ","  + x.longitude;
                            Intent intent = new Intent(MapsActivity.this,Details.class);
                            intent.putExtra("loc",latlong);
                            startActivityForResult(intent,0);
                        }
                    });
                }
                return true;
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Context context = getApplicationContext();
            CharSequence text = "EatSafely: onProcessingUpdate";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}

