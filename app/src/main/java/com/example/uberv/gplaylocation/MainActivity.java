package com.example.uberv.gplaylocation;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        ,LocationListener
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int ERROR_DIALOG_REQUEST = 2007;
    public static final int LOCATION_PERMISSION_REQUEST = 4008;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private boolean mReceivingLocationUpdates;
    private boolean mAskedForPermission;

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
            // some devices do not have Google Play Services

            // Create an instance of GoogleAPIClient.
            if (mGoogleApiClient == null) {
                // connect to Googly Play Services
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API) // specify, which APIs we want
                        .build();
            }
            // Create the LocationRequest object
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000) // 1 second, in milliseconds
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            // PRIORITY_HIGH_ACCURACY - use GPS to get most accurate location
            // PRIORITY_BALANCED_POWER - accuracy of about 100 meters
            // PRIORITY_NO_POWER - receive location updates when another app is receiving them

        }
    }

    private void checkGPS(){
        LocationManager locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG,"GPS Disabled!");

            final Snackbar bar = Snackbar.make(mRootLayout,"No GPS signal",Snackbar.LENGTH_INDEFINITE);
            bar.setAction("TURN ON GPS", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    bar.dismiss();
                }
            });

            bar.show();
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

    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop(){
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check gps
        LocationManager locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG,"GPS Disabled!");

            final Snackbar bar = Snackbar.make(mRootLayout,"No GPS signal",Snackbar.LENGTH_INDEFINITE);
            bar.setAction("TURN ON GPS", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    bar.dismiss();
                }
            });

            bar.show();
        }
    }

    private void checkLocationSettings(){
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
                        Log.d(TAG,"Resolution required");
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        Snackbar.make(mRootLayout,"GPS is unavailable!",Snackbar.LENGTH_LONG).setAction("TURN ON"
                                , new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    status.startResolutionForResult(
                                            MainActivity.this,
                                            2008);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        Log.d(TAG,"Requesting resolution.");

                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Log.d(TAG,"cannot fix");
                        break;
                }
            }
        });
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
                    //trigger onConnected again
                    onConnected(null);
                } else {
                    Toast.makeText(this, "Permissions is required to access your location!", Toast.LENGTH_SHORT).show();
                    // do not spam
                    mAskedForPermission=true;
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

        if(isLocationAllowed()) {
            // get last known location
            Location location = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            if (location == null) {
                // LastLocation is null.
                //noinspection MissingPermission
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                mReceivingLocationUpdates=true;
            } else {
                handleNewLocation(location);
            }
        }
    }

    private boolean isLocationAllowed(){
        // check permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Lack of permissions");
            // We do not have location permissions
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_CONTACTS))
            {
                // We should show user and explanation message

                Snackbar.make(mRootLayout,"Location permission is required!",Snackbar.LENGTH_LONG)
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
                if(!mAskedForPermission) {
                    String[] permissions = new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION};
                    ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST);
                }
            }
            // do not proceed further if no permission
            return false;
        }
        return true;
    }

    private void handleNewLocation(Location location){
        Log.d(TAG,"handling location: "+location.toString());

        double lat=location.getLatitude();
        double lng=location.getLongitude();
        mLatitudeText.setText(String.valueOf(lat));
        mLongtitudeText.setText(String.valueOf(lng));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"onConnectionSuspended()");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
            new AlertDialog.Builder(this)
                    .setMessage("Connection failed. Error code: " +
                            connectionResult.getErrorCode())
                    .show( );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // fom gps dialog. -1 = yes, 0 = no
        Log.d(TAG,""+requestCode+", "+resultCode);

        if(requestCode==CONNECTION_FAILURE_RESOLUTION_REQUEST){
            // startResolutionForResult - turn on GPS/WIFI
            if(resultCode==-1){
                Log.d(TAG,"Settings fixed");
//                getLastLocation();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
        // if we need to get valid location only one

    }
}
