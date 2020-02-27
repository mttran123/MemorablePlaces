package com.example.gohasu.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    String address = "";


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

//                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                updateLocationInfo(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();
        int x = intent.getIntExtra("place", 0);

        // Zoom in the user location

        if(x == 0) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    updateLocationInfo(location.getLatitude(), location.getLongitude());

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

//                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                updateLocationInfo(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            }

        } else {
            LatLng clickedLatLng = MainActivity.locations.get(x);

            updateLocationInfo(clickedLatLng.latitude, clickedLatLng.longitude);
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                updateLocationInfo(latLng.latitude, latLng.longitude);
                Toast.makeText(MapsActivity.this, "Location Saved!", Toast.LENGTH_SHORT).show();

//                returnIntent = new Intent(getApplicationContext(), MainActivity.class);
//                returnIntent.putExtra("address", address);

                MainActivity.listOfPlaces.add(address);
                MainActivity.locations.add(latLng);

                MainActivity.arrayAdapter.notifyDataSetChanged();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.gohasu.memorableplaces", Context.MODE_PRIVATE);

                try {

                    ArrayList<String> latitudes = new ArrayList<>();
                    ArrayList<String> longitudes = new ArrayList<>();

                    for(LatLng coord : MainActivity.locations) {
                        latitudes.add(Double.toString(coord.latitude));
                        longitudes.add(Double.toString(coord.longitude));

                    }

                    sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.listOfPlaces)).apply();
                    sharedPreferences.edit().putString("latitudes", ObjectSerializer.serialize(latitudes)).apply();
                    sharedPreferences.edit().putString("longitudes", ObjectSerializer.serialize(longitudes)).apply();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

//    @Override
//    public void onBackPressed() {
//        startActivity(returnIntent);
//    }

    public void updateLocationInfo(Double latitude, Double longitude) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if(addresses.size() >0 && addresses != null) {
                address = addresses.get(0).getAddressLine(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        LatLng currentLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(currentLocation).title(address));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
    }
}


