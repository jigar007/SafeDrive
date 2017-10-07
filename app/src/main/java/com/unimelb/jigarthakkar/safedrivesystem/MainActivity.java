package com.unimelb.jigarthakkar.safedrivesystem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.graphics.Color;
import android.os.Handler;

import com.google.android.gms.vision.face.Face;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
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



public class MainActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        ConnectionCallbacks,
        LocationListener,
        SensorEventListener
{
    ArrayList<Double> tmp = new ArrayList<Double>();
    private Button record;
    private Button contact;
    private Button profile;
    private Button face;
    private boolean onRoad = false;
    private GoogleMap map;
    private boolean msgSent = false;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationManager mLocationManager;
    protected Location currentLocation;
    protected Marker currentMarker;
    protected LocationRequest locationRequest;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private boolean mapLoaded = false;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    private ArrayList<Double> aveAcce = new ArrayList<Double>();
    private SensorManager sensorManager;
    private LineGraphSeries<DataPoint> series;
    private static double currentX;
    private ThreadPoolExecutor liveChartExecutor;
    private LinkedBlockingQueue<Double> accelerationQueue = new LinkedBlockingQueue<>(10);
    private ArrayList<String> visitedAddress = new ArrayList<String>();


    protected synchronized void buildGoogleApiClient() {
        // buile a new client instance to call google map api
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void buildLocationRequest() {
        // settings to get the information about the location

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
         *  The main activity should contain following components:
         *  Button to jump to SOSActivity
         *  Button to jump to RecordActivity
         *  Button to jump to ProfileActivity
         *  Google map, which is used for showing current location and orientation
         *  GraphView, which is used for showing current acceleration of the car
         *  And we need to register accelerometer and magnetic field sensor to detect the
         *  acceleration and orientation of the user
         */

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.Map);
        fragment.getMapAsync(MainActivity.this);


        // set the button to jump to SOSActivity
        contact = (Button)findViewById(R.id.SOS);
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, SOSActivity.class);
                startActivity(intent);
            }
        });

        // set the button to jump to RecordActivity
        record = (Button)findViewById(R.id.Record);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                startActivity(intent);
            }
        });

        // set the button to jump to ProfileActivity
        profile = (Button)findViewById(R.id.face);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FaceActivity.class);
                startActivity(intent);
            }
        });

        // set the button to jump to FaceActivity
        profile = (Button)findViewById(R.id.Profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // register sensors
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer,
                SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_UI);


        // build the client instance and setting to use GoogleMap API
        buildGoogleApiClient();
        buildLocationRequest();
        currentLocation = getCurrentLocation();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        // initialize the GraphView
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        //graph.setTitle("current acceleration");
        graph.setTitleColor(Color.RED);
        //graph.setTitleTextSize(100);


        graph.setBackgroundColor(Color.argb(50, 0, 0, 0));
        series = new LineGraphSeries<>();
        //series.setColor(R.color.deepblue);
        series.setColor(Color.BLUE);
        series.setThickness(5);
        graph.addSeries(series);
        // activate horizontal zooming and scrolling
        graph.getViewport().setScalable(true);

        // activate horizontal scrolling
        graph.getViewport().setScrollable(true);

        // activate horizontal and vertical zooming and scrolling
        graph.getViewport().setScalableY(true);

        // activate vertical scrolling
        graph.getViewport().setScrollableY(true);
        // To set a fixed manual viewport use this:
        // set manual X bounds

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0.1);
        graph.getViewport().setMaxX(5);
        //graph.getViewport().setMaxXAxisSize(100);
        //graph.getViewport().setMaxX(20);

        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(8);
        currentX = 0;
        // Start chart thread
        liveChartExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        if (liveChartExecutor != null)
            liveChartExecutor.execute(new AccelerationChart(new AccelerationChartHandler()));
    }

    @Override
    public void onStart() {
        // start to connect to GooglePlay service
        super.onStart();
        mGoogleApiClient.connect();
    }

    public void onStop() {
        // stop connection to GooglePlay service
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // if connection failed, print the error code
        int errorCode = connectionResult.getErrorCode();
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Toast.makeText(this, "Connection failed !", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        /*
         * Once the readings of the accelerometer and magnetic sensor are changed, we can calculate
         * the current orientation of the user based on these readings.
         * Also, if there is a sudden brake or car accident, the system would automatically send a
         * sms message to the emergent contact to get help. this action is based on the reading of
         * the accelerometer(standard deviation of the last 10 reading of the acceleration)
         */
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mAccelerometerReading,
                    0, mAccelerometerReading.length);
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading,
                    0, mMagnetometerReading.length);
        }
        updateOrientationAngles();
        float angle0 = mOrientationAngles[0];
        if (mapLoaded) {
            if (mGoogleApiClient != null && currentLocation != null && currentMarker != null) {
                // show the orientation
                currentMarker.setRotation((((angle0)*180/(3.14f)) * 10) / 10);
            }
        }
        if (aveAcce.size() > 10) {
            /*
             * get the last 10 reading of the accelerometer, and calculate the standard deviation,
             * if the reading exceed the thereshold, the system would automatically send a SMS
             * message to the emergency contact
             */
            aveAcce.remove(0);
            double standardDev = getStandardDev(aveAcce);
            //Log.d("Dev", String.valueOf(standardDev));
            if ((standardDev > 80) && (msgSent == false) ) {
                try {
                    // send the message
                    String phoneNo = "+61450561102";
                    String msg = "haha";
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, msg, null, null);
                    msgSent = true;
                    Toast.makeText(getApplicationContext(), "Message Sent",
                            Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                            Toast.LENGTH_LONG).show();
                    ex.printStackTrace();
                }

            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onResume() {
        /*
         * if the application is activated from the background, re-register the sensor and
         * reconnect to the GooglePlay service
         */
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer,
                SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_UI);
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
        // move the map so that the cursor of the user would stay center in the map
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(17)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void drawMarker(Location location) {
        // draw a marker on the current position on the map
        if (map != null) {
            map.clear();
            LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions cur_op = new MarkerOptions().position(gps).title("current location")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_navigation_black_24dp))
                    .flat(true)
                    .anchor(0.5f, 0.5f);
            currentMarker = map.addMarker(cur_op);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 12));
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        /*
         * once the location is changed, we can get the current address to decide whether the user
         * is on the road. and we refresh the map to show the current position
         */

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;
        drawMarker(location);
        moveMap(latLng);
        currentLocation = location;
        mapLoaded = true;
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        try {
            List<Address> address = geocoder.getFromLocation(latitude, longitude, 1);
            String addressString = address.get(0).getAddressLine(0) + " ";
            String city = address.get(0).getLocality() + " ";
            String state = address.get(0).getAdminArea() + " ";
            String country = address.get(0).getCountryName() + " ";

            String tmp = addressString + city + state;
            if (!visitedAddress.contains(tmp)) {
                visitedAddress.add(tmp);
            }

            if (addressString.contains("Rd")) {
                onRoad = true;
            }
            else {onRoad = false;}
            SharedPreferences sharedPreferences = getSharedPreferences("test", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("currentLocation", addressString + city + state + country);

            editor.commit();
            editor.remove("visitedLocation").commit();

            for (String s : visitedAddress) {
                editor.putString("visitedLocation", s + "\n");
            }
            editor.commit();

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

        // return current location
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
        // determine the orientation of the device based on the sensors

        mSensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);
        float tmp = mAccelerometerReading[0];
        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
    }

    public double sumArray(ArrayList<Double> x) {
        if (x == null) {return 0;}
        double res = 0;
        for (double each : x) {
            res += each;
        }
        return res;
    }

    public double getStandardDev(ArrayList<Double> x) {
        double ave = sumArray(x) / x.size();
        double tmp = 0;
        for (double each : x) {
            tmp += Math.pow((each - ave), 2);
        }
        return (tmp / x.size());
    }

    private class AccelerationChartHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // read the current reading of the accelerometer and draw the graph
            Double accelerationY = 0.0D;
            if (!msg.getData().getString("ACCELERATION_VALUE").equals(null) && !msg.getData().getString("ACCELERATION_VALUE").equals("null")) {
                accelerationY = (Double.parseDouble(msg.getData().getString("ACCELERATION_VALUE")));
            }
            series.appendData(new DataPoint(currentX, accelerationY), true, 100);
            currentX = currentX + 0.05;

            tmp.add(accelerationY);
            Log.d("avesize", String.valueOf(aveAcce.size()));
            if (tmp.size() > 10) {
                aveAcce.add(sumArray(tmp));
            }
        }
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        double x = values[0];
        double y = values[1];
        double z = values[2];
        double accelerationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        double acceleration = Math.sqrt(accelerationSquareRoot);
        accelerationQueue.offer(acceleration);
    }

    private class AccelerationChart implements Runnable {
        private boolean drawChart = true;
        private Handler handler;
        public AccelerationChart(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {

            while (drawChart) {
                // generate a new thread to draw the chart
                Double accelerationY;
                try {
                    Thread.sleep(120); // Speed up the X axis
                    accelerationY = accelerationQueue.poll();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                if (accelerationY == null)
                    continue;
                // currentX value will be excced the limit of double type range
                // To overcome this problem comment of this line
                 //currentX = (System.currentTimeMillis() / 1000) * 8 + 0.6;
                Message msgObj = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("ACCELERATION_VALUE", String.valueOf(accelerationY));
                msgObj.setData(b);
                handler.sendMessage(msgObj);
            }
        }
    }
}

