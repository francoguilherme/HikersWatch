package com.guilherme.hikerswatch;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.lang.Math;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    LocationManager locationManager;
    String locationProvider;
    Location location;
    Criteria criteria;

    Boolean isPermissionGranted = false;

    TextView latitudeView, longitudeView, accuracyView, speedView,
            bearingView, altitudeView, addressView;

    String speedInKmPerHour;
    String shortAccuracy; // Used to show accuracy with only 2 decimal places
    Geocoder geocoder;

    public void findViews(){

        latitudeView = findViewById(R.id.latitudeView);
        longitudeView = findViewById(R.id.longitudeView);
        accuracyView = findViewById(R.id.accuracyView);
        speedView = findViewById(R.id.speedView);
        bearingView = findViewById(R.id.bearingView);
        altitudeView = findViewById(R.id.altitudeView);
        addressView = findViewById(R.id.addressView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else{

            // Permission already granted
            isPermissionGranted = true;
        }

        if (!isPermissionGranted){
            return;
        }

        locationProvider = locationManager.getBestProvider(criteria, false);

        location = locationManager.getLastKnownLocation(locationProvider);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            isPermissionGranted = true;

            // Get provider and last known location right after granting permission

            locationProvider = locationManager.getBestProvider(criteria, false);

            try{

                location = locationManager.getLastKnownLocation(locationProvider);

            } catch (SecurityException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isPermissionGranted){
            return;
        }

        locationProvider = locationManager.getBestProvider(criteria, false);

        try{

            locationManager.requestLocationUpdates(locationProvider, 5000, 1, this);

        } catch(SecurityException e){
            e.printStackTrace();
        }

        onLocationChanged(location);
    }

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
    }

    public String getBearingInCardinal(float degrees){

        String[] cardinals = { "N", "NE", "E", "SE", "S", "SW", "W", "NW", "N" };
        return cardinals[ (int)Math.round(((double)degrees % 360) / 45) ];
    }

    public String getAddress(){

        List<Address> addressList;

        Double lat = location.getLatitude();
        Double lng = location.getLongitude();
        String country, state, city, street;

        try {

            addressList = geocoder.getFromLocation(lat, lng, 1);

            if (addressList == null && addressList.size() == 0){

                return "Address not found";
            }

            country = addressList.get(0).getCountryName();
            state = addressList.get(0).getAdminArea();
            city = addressList.get(0).getSubAdminArea();
            street = addressList.get(0).getThoroughfare();

            return street + "\n" + city + "\n" + state + ", " + country;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Address not found";
    }

    @Override
    public void onLocationChanged(Location location) {

        latitudeView.setText("Latitude: " + location.getLatitude());
        longitudeView.setText("Longitude: " + location.getLongitude());

        shortAccuracy = String.format("%.2f", location.getAccuracy());
        accuracyView.setText("Accuracy: " + shortAccuracy + " meters");

        speedInKmPerHour = String.format("%.2f", location.getSpeed() * 3.6);
        speedView.setText("Speed: " + speedInKmPerHour + " kph");

        bearingView.setText("Bearing: " + getBearingInCardinal(location.getBearing()));
        altitudeView.setText("Altitude: " + location.getAltitude() + " meters");

        addressView.setText(getAddress());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
