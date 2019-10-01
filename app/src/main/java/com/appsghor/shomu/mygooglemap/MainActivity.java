package com.appsghor.shomu.mygooglemap;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.appsghor.shomu.mygooglemap.util.PermissionsUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        , LocationListener {

    GoogleMap mGoogleMap;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this); /// add for play services 10.0.1
        super.onCreate(savedInstanceState);
        if (isGoogleServicesAvailable()) {
            setContentView(R.layout.activity_main);
            // startActivity(new Intent(this,Main2Activity.class));
            //startActivity(new Intent(this,PathGoogleMapActivity.class));
            // startActivity(new Intent(this,DrawingDrivingRoute.class));
            //startActivity(new Intent(this,PlacesAutocompleteCustomSuggestions.class));


            List<String> permissionsList = new ArrayList<>();
            permissionsList.add(Manifest.permission.CAMERA);

            PermissionsUtil.checkPermissions(this, permissionsList, isGranted -> {
                if (isGranted) {
                    initatizeMap();
                } else {
                    Timber.e("Permissions is Granted:" + isGranted);
                }
            });


        } else {
            ///implement other     set other layout (google map if unsupported)
        }


        findViewById(R.id.ok_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    geoLocate();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public void initatizeMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    public boolean isGoogleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvaileble = api.isGooglePlayServicesAvailable(this);
        if (isAvaileble == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvaileble)) {
            Dialog dialog = api.getErrorDialog(this, isAvaileble, 0);  ///if device need upgrade google play service updete
            dialog.show();
        } else {
            Toast.makeText(this, "Cann't Connect to Play Services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;


        //goToLocation(23.7991531,90.4285118);
        // goToLocationWithZoom(23.7991531,90.4285118,15);  //23.7991531,90.4285118  University of Information Technology and Sciences

/*
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.GET_PERMISSIONS){

            }

        }

        mGoogleMap.setMyLocationEnabled(true);
*/
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();
    }

    private void goToLocation(double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    private void goToLocationWithZoom(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    public void geoLocate() throws IOException {
        EditText searchET = (EditText) findViewById(R.id.search_et);
        String location = searchET.getText().toString();
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList = geocoder.getFromLocationName(location, 1);

        Address address = addressList.get(0);
        String locality = address.getLocality();
        Toast.makeText(this, "locality: " + locality, Toast.LENGTH_LONG).show();


        double lat = address.getLatitude();
        double lng = address.getLongitude();
        goToLocationWithZoom(lat, lng, 15);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(mGoogleMap==null){
            return false;
        }

        switch (item.getItemId()) {

            case R.id.map_type_none:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;

            case R.id.map_type_normal:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;

            case R.id.map_type_satellite:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;

            case R.id.map_type_terrain:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;

            case R.id.map_type_hybrid:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.GET_PERMISSIONS) {

            }

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, MainActivity.this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (location == null) {
            Toast.makeText(this, "Cann't Get current Location: ", Toast.LENGTH_LONG).show();
        } else {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mGoogleMap.animateCamera(cameraUpdate);
        }
    }

}
