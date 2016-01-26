package hue.recon.facebook;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by pawanD on 1/23/2016.
 */
public class locationUpdateService extends IntentService {
    Location location;
    MainActivity m = new MainActivity();
    private String cordX;
    private String cordY;

    public void onDestroy (){
        Toast.makeText(getApplicationContext(),"locationUpdateService/onDestroy" + "Service Destroyed!",Toast.LENGTH_SHORT).show();
    }

    public locationUpdateService() {

        super("locationUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.hasExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED) && !m.userID.equals(null)) {

            location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
            Log.d("locationtesting", "accuracy: " + location.getAccuracy() + " lat: " + location.getLatitude() + " lon: " + location.getLongitude());

            cordX = Double.toString(location.getLatitude());
            cordY = Double.toString(location.getLongitude());
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
            query.whereEqualTo("user", m.userID);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException error) {
                    if (error == null) {
                        object.put("user", m.userID);
                        object.put("cordX", cordX);
                        object.put("cordY", cordY);
                        object.saveInBackground();
                        Toast.makeText(getApplicationContext(), "locationUpdateService/onHandleIntent" + "(cords sent!)", Toast.LENGTH_SHORT).show();
                    } else {
                        if (error.getCode() == error.OBJECT_NOT_FOUND)
                        {
                            Toast.makeText(getApplicationContext(), "locationUpdateService/onHandleIntent  "+ error.toString() + "(OBJECT_NOT_FOUND)", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "locationUpdateService/onHandleIntent "+ error.toString() + "(maybe internet issue)", Toast.LENGTH_SHORT).show();
                        }
                        //
                    }
                }
            });
        }
    }
}
