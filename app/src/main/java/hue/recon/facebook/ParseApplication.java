package hue.recon.facebook;

import android.app.Application;

import com.parse.Parse;


/**
 * Created by pawanD on 1/24/2016.
 */
public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this);
    }
}
