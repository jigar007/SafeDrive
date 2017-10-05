package com.unimelb.jigarthakkar.safedrivesystem;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

//import android.location.LocationListener;


public class MainActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        ConnectionCallbacks,
        LocationListener,
        SensorEventListener
{
    private Button record;
    private Button contact;
    private Button profile;
    private boolean onRoad = false;
    private GoogleMap map;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected LocationManager mLocationManager;
    protected LocationListener mLocationListener;
    protected Location currentLocation;
    protected Marker currentMarker;
    protected LocationRequest locationRequest;
    protected Sensor mSensor;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private boolean mapLoaded = false;
    private static final float filterFactor = 0.9f;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.Map);
        fragment.getMapAsync(MainActivity.this);
        GoogleMap curMap = map;
        record = (Button)findViewById(R.id.Record);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                startActivity(intent);
                // MainActivity.this.finish();
            }
        });

        profile = (Button)findViewById(R.id.Profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);

        contact = (Button)findViewById(R.id.SOS);
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, SOSActivity.class);
                startActivity(intent);
            }
        });

        buildGoogleApiClient();
        buildLocationRequest();
        currentLocation = getCurrentLocation();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Toast.makeText(this, "Connection failed !", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //Toast.makeText(this, "sensor changed", Toast.LENGTH_LONG).show();
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] filtered = new float[3];
            filtered = lowPassFilter(event.values.clone(), filtered);
            System.arraycopy(filtered, 0, mAccelerometerReading,
                    0, mAccelerometerReading.length);
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float[] filtered = new float[3];
            filtered = lowPassFilter(event.values.clone(), filtered);
            System.arraycopy(filtered, 0, mMagnetometerReading,
                    0, mMagnetometerReading.length);
        }
        updateOrientationAngles();
        float angle0 = mOrientationAngles[0];
        float angle1 = mOrientationAngles[1];
        float angle2 = mOrientationAngles[2];

        float r1 = mRotationMatrix[0];
        float r2 = mRotationMatrix[1];
        float r3 = mRotationMatrix[2];

        if (mapLoaded) {
            if (mGoogleApiClient != null && currentLocation != null && currentMarker != null) {
                //drawMarker(currentLocation);
                //LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                //moveMap(latLng);
                currentMarker.setRotation((((angle0)*180/(3.14f)) * 10) / 10);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(this, "back", Toast.LENGTH_LONG).show();
        //dd


        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);



        if (!mGoogleApiClient.isConnected() && currentMarker != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        mSensorManager.unregisterListener(this);
    }

    private void moveMap(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(17)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void drawMarker(Location location) {
        if (map != null) {
            map.clear();
            LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions cur_op = new MarkerOptions().position(gps).title("current location")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_navigation_black_24dp))
                    .flat(true)
                    .anchor(0.5f, 0.5f);
            //.rotation(mOrientationAngles[2] * 100);

            currentMarker = map.addMarker(cur_op);


            map.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 12));
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        String whetherOnRoad = "on road : " + Boolean.toString(onRoad);
        Toast.makeText(this, whetherOnRoad, Toast.LENGTH_LONG).show();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;
        drawMarker(location);
        moveMap(latLng);
        currentLocation = location;
        mapLoaded = true;

        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        try {
            List address = geocoder.getFromLocation(latitude, longitude, 1);
            String addressString = address.toString();
            if (addressString.contains("Road")) {
                onRoad = true;
            }
            else {onRoad = false;}

            Toast.makeText(this, address.toString(), Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            Toast.makeText(this, "can not find address !", Toast.LENGTH_LONG).show();
        }



    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, MainActivity.this);
        }
    }

    @Override
    public void onConnectionSuspended(int i ) {}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }


    public Location getCurrentLocation() {
        Location cur = null;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //cur = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            cur = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        }
        return cur;

    }

    public void updateOrientationAngles() {
        mSensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);
        float tmp = mAccelerometerReading[0];
        String s =String.valueOf(tmp);
        Log.d("asd",s);

        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
    }

    public float[] lowPassFilter(float[] input, float[] output) {
        if (input == null) {return output;}
        for (int i = 0; i < input.length; i++) {
            output[i] = ((output[i] + filterFactor * (input[i] - output[i])) * 10) / 10;
        }
        return output;

    }



}



