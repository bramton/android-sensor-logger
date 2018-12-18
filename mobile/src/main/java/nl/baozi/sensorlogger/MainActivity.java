package nl.baozi.sensorlogger;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "blaat.main";
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Log.i(TAG, "main onCreate called");

        // Get available sensors on device
        SensorManager mSensorManager;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (int i=0; i < deviceSensors.size(); i++) {
            //Log.i(TAG, deviceSensors.get(i).getStringType());
            Log.i(TAG, deviceSensors.get(i).getName());
            //Log.i(TAG, deviceSensors.get(i).getVendor());
            //Log.i(TAG,"---");
        }

        Sensor mAccSensor = null;
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccSensor == null) {
            Log.e(TAG, "Could not find accelerometer.");
        }
        BramSensorEventListener AccListener = new BramSensorEventListener();
        mSensorManager.registerListener(AccListener, mAccSensor, 100);


        // Source: https://medium.com/@droidbyme/get-current-location-using-fusedlocationproviderclient-in-android-cb7ebf5ab88e
        LocationCallback locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locRes) {
                Log.i(TAG,"callback called");
                if (locRes == null) {
                    return;
                }

                for (Location location: locRes.getLocations()) {
                    if (location == null) {
                        continue;
                    }
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    long ts = location.getElapsedRealtimeNanos();
                    Log.i(TAG,"lat: "+lat+"  lon: "+lon);
                }
            }
        };

        // Check permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG,"wel permission");
        } else {
            Log.i(TAG,"geen permission");

        }

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Check the settings
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Log.i(TAG,"Everything looks fine. Carry on.");
                } catch (ApiException e) {
                    Log.i(TAG,"Uh oh");
                    Log.i(TAG,"Status code:"+e.getStatusCode());
                    Log.i(TAG,e.getMessage());
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings ");
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sie) {
                                Log.i(TAG, "PendingIntent unable to execute request.");
                            }
                            break;
                    }
                }
            }
        });

        FusedLocationProviderClient mFusedLocationClient;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallBack, null);
        } else {
            Log.i(TAG,"geen permission");

        }








        //Task<List<Node>> nodesTask = Wearable.getNodeClient(this).getConnectedNodes();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        //mRequestingLocationUpdates = false;
                        //updateUI();
                        break;
                }
                break;
        }
    }

}
