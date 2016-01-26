package hue.recon.facebook;

import android.app.Activity;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pawan on 1/22/2016.
 */
public class locate_activity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, android.hardware.SensorEventListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final String LOG_TAG = "pawantestlog";
    private TextView cordXField;
    private TextView cordYField;
    private String cordX;
    private String cordY;
    MainActivity m = new MainActivity();
    private GoogleMap googleMap;
    private LatLng userLocation;
    float[] mRotationMatrix = new float[16];
    float mDeclination;
    SensorManager mSensorManager;
    Sensor mRotVectSensor;
    ArrayList<Marker> markerList = new ArrayList<Marker>(10);
    float angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locate_activity);
        cordXField = (TextView) findViewById(R.id.cordX);
        cordYField = (TextView) findViewById(R.id.cordY);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mRotVectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorManager.registerListener(this, mRotVectSensor, 40000);

        try {
            if (googleMap == null) {
                googleMap = ((MapFragment) getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        }
        catch (Exception e) {
            e.printStackTrace();
        }


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    @Override
    public void onConnected(Bundle bundle){
        Toast.makeText(this,"onConnected",Toast.LENGTH_SHORT).show();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(50);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F);

        // create the Intent to use WebViewActivity to handle results
        //Intent mRequestLocationUpdatesIntent = new Intent(this, locationUpdateService.class);

        // create a PendingIntent
        //PendingIntent mRequestLocationUpdatesPendingIntent = PendingIntent.getService(getApplicationContext(), 0,
          //      mRequestLocationUpdatesIntent,
            //    PendingIntent.FLAG_UPDATE_CURRENT);

        // request location updates

        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
        //        mLocationRequest,
        //        mRequestLocationUpdatesPendingIntent);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest,
                (LocationListener) this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOG_TAG, location.toString());
        cordXField.setText(Double.toString(location.getLatitude()));
        cordYField.setText(Double.toString(location.getLongitude()));
        cordX = Double.toString(location.getLatitude());
        cordY = Double.toString(location.getLongitude());
        Toast.makeText(this,"onLocationChanged Triggered!",Toast.LENGTH_SHORT).show();
        googleMap.clear();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
        query.whereNotEqualTo("user", m.userID);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objectList, ParseException error) {
                if (error == null) {
                    for (ParseObject object : objectList) {
                        String title = object.get("user").toString();
                        String Lat = object.get("cordX").toString();
                        String Lng = object.get("cordY").toString();
                        googleMap.addMarker(new MarkerOptions().
                                position(new LatLng(Double.valueOf(Lat), Double.valueOf(Lng))).title(title).flat(false));
                        Toast.makeText(getApplicationContext(), "new marker placed!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    //
                }
            }
        });

        Toast.makeText(getApplicationContext(), "user marker!", Toast.LENGTH_SHORT).show();
        userLocation = new LatLng(location.getLatitude(),location.getLongitude());

        final Marker UL = googleMap.addMarker(new MarkerOptions().
                position(userLocation).title(m.userID).flat(false).icon(BitmapDescriptorFactory.fromResource(R.mipmap.arrow)).alpha((float) 1.0));


        if(markerList.size()==10) {markerList.remove(0);markerList.add(UL);}
        else {markerList.add(UL);}

        float i = (float)0.1;
        for(Marker x: markerList){
            googleMap.addMarker(new MarkerOptions().
                    position(x.getPosition()).title(m.userID).flat(false).icon(BitmapDescriptorFactory.fromResource(R.mipmap.arrow)).alpha(i));
            i += 0.1;
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
        .target(userLocation)      // Sets the center of the map to Mountain View
        .zoom(18)                   // Sets the zoom
        .bearing(90)                // Sets the orientation of the camera to east
        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        GeomagneticField field = new GeomagneticField(
                (float)location.getLatitude(),
                (float)location.getLongitude(),
                (float)location.getAltitude(),
                System.currentTimeMillis()
        );

        // getDeclination returns degrees
        mDeclination = field.getDeclination();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mRotVectSensor, 40000);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mRotationMatrix, orientation);
            if (Math.abs(Math.toDegrees(orientation[0]) - angle) > 0.8) {
                float bearing = (float) Math.toDegrees(orientation[0]) + mDeclination;
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(userLocation)
                        .zoom(18)
                        .tilt(30)
                        .bearing(bearing)                // Sets the orientation of the camera to east
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
            angle = (float)Math.toDegrees(orientation[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void updateCamera(float bearing) {
        CameraPosition oldPos = googleMap.getCameraPosition();
        CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
    }


    @Override
    public void onConnectionSuspended(int i){
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }
}
