package com.example.uberv.gplaylocation;

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
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int ERROR_DIALOG_REQUEST = 2007;
    public static final int LOCATION_PERMISSION_REQUEST = 4008;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

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

    // --- GoogleApiClient callbacks ---
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected()");
        Toast.makeText(this, "Sucessfully connected to the Google Play Services", Toast.LENGTH_SHORT).show();

        getLastLocation();
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
