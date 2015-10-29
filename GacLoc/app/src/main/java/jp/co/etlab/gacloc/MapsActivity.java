package jp.co.etlab.gacloc;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private GoogleMap mMap = null;
    private Location location;
    private TextView textView;

    private GoogleApiClient mLocationClient = null;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(60000)
            .setFastestInterval(10000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        textView = (TextView)findViewById(R.id.textView);
        mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if(mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
        mLocationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if(mLocationClient != null){
            mLocationClient.connect();
        }
    }
    @Override
    public void onConnected(Bundle connectionHint){
        fusedLocationProviderApi.requestLocationUpdates(mLocationClient, REQUEST, this);
        Location currentLocation = fusedLocationProviderApi.getLastLocation(mLocationClient);
        if (currentLocation != null && (System.currentTimeMillis()-currentLocation.getTime()) > 20000) {
            location = currentLocation;
            dispLoc(location, textView);
            CameraPosition cameraPos = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(),location.getLongitude())).zoom(17.0f)
                    .bearing(0).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
        } else {
            fusedLocationProviderApi.requestLocationUpdates(mLocationClient, REQUEST, this);
            // Schedule a Thread to unregister location listeners
            Executors.newScheduledThreadPool(1).schedule(new Runnable() {
                @Override
                public void run() {
                    fusedLocationProviderApi.removeLocationUpdates(mLocationClient, MapsActivity.this);
                }
            }, 5000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onConnectionSuspended(int cause){
    }

    @Override
    public void onLocationChanged(Location loc) {
        if(location ==null) {
            CameraPosition cameraPos = new CameraPosition.Builder()
                    .target(new LatLng(loc.getLatitude(),loc.getLongitude())).zoom(17.0f)
                    .bearing(0).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
        }
        location = loc;
        dispLoc(loc,textView);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void dispLoc(Location loc,TextView tx) {
        String textLog = "";

        textLog = "\n";
        textLog += "Latitude="+ numToDeg(loc.getLatitude())+"\n";
        textLog += "Longitude="+ numToDeg(loc.getLongitude())+"\n";
        textLog += "Accuracy="+ String.valueOf(loc.getAccuracy())+"\n";
        textLog += "Altitude="+ String.valueOf(loc.getAltitude())+"\n";
        textLog += "Time="+ String.valueOf(loc.getTime())+"\n";
        textLog += "Speed="+ String.valueOf(loc.getSpeed())+"\n";
        textLog += "Bearing="+ String.valueOf(loc.getBearing())+"\n";
        tx.setText(textLog);

    }
    private String numToDeg(double latlog) {
        int hour;
        int minu;
        double minuW;
        int sec;

        hour = (int) latlog;
        minuW = (latlog-(double)hour)*60.0;
        minu = (int)minuW;
        sec =(int)((minuW-(double)minu)*60.0);
        return hour + "°" + minu + "'" + sec + "''";
    }
}