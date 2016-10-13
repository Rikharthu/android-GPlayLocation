package com.example.uberv.gplaylocation;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int ERROR_DIALOG_REQUEST = 2007;
    public static final int LOCATION_PERMISSION_REQUEST = 4008;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    private RelativeLayout mRootLayout;
    private TextView mLatitudeText, mLongtitudeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRootLayout= (RelativeLayout) findViewById(R.id.root_layout);
        mLatitudeText = (TextView) findViewById(R.id.latitude);
        mLongtitudeText = (TextView) findViewById(R.id.longtitude);

        if (isGPlayServiceAvailable()) {
            // Create an instance of GoogleAPIClient.
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }
        }
    }

    private boolean isGPlayServiceAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                // user can take action to fix situation
                googleAPI.getErrorDialog(this, result,
                        ERROR_DIALOG_REQUEST).show();
            }

            return false;
        }
        // everything is OK
        return true;
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(mListener!=null) {
//            LocationRequest request = LocationRequest.create();
//            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//            request.setInterval(5000); // refresh duration in ms
//            request.setFastestInterval(1000);
//            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, request, mListener);
//        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // get current location settings
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        // Check whether the location settings are satisfied
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        // TODO extract as implemented interface
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationSettingsStates= result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Log.d(TAG,"Success!");
                        // TODO

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    2008);
                            Log.d(TAG,"Requesting resolution.");
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // fom gps dialog. -1 = yes, 0 = no
        Log.d(TAG,""+requestCode+", "+resultCode);

        if(requestCode==2008){
            // startResolutionForResult - turn on GPS/WIFI
            if(resultCode==-1){
                Log.d(TAG,"Settings fixed");
//                getLastLocation();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!

                    // request last location again
                    getLastLocation();
                } else {
                    Toast.makeText(this, "Permissions is required to access your location!", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    // --- GoogleApiClient callbacks ---
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected()");
        Toast.makeText(this, "Sucessfully connected to the Google Play Services", Toast.LENGTH_SHORT).show();

        getLastLocation();
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // We do not have location permissions
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_CONTACTS))
            {
                // We should show user and explanationmessage

                Snackbar.make(mRootLayout,"Location permission is requred!",Snackbar.LENGTH_LONG)
                        .setAction("Allow", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // ask for permission if "Allow" button is clicked
                                String[] permissions = new String[]{
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION};
                                ActivityCompat.requestPermissions(MainActivity.this, permissions, LOCATION_PERMISSION_REQUEST);
                            }
                        })
                        .show();
            } else {
                // request for permission
                String[] permissions = new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST);
            }
            // do not proceed further if no permission
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            double lat=mLastLocation.getLatitude();
            double lng=mLastLocation.getLongitude();
            Log.d(TAG,lat+", "+lng);
            mLatitudeText.setText(String.valueOf(lat));
            mLongtitudeText.setText(String.valueOf(lng));
        }

        // subscribe for further updates
        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"onConnectionFailed() "+connectionResult.getErrorMessage());
    }
}
