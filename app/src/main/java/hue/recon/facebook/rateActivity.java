package hue.recon.facebook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Random;

/**
 * Created by pawan on 1/20/2016.
 */
public class rateActivity extends AppCompatActivity {
    private String userID;
    private String userName;
    private int pointer;
    private int usernumber;
    private ImageView ratable;
    private SeekBar rating;
    private TextView numrating;
    MainActivity m = new MainActivity();
    private String oldrating;
    private int numraters;
    private boolean notrated;
    private Button nextButton;
    private Button skipButton;
    private long mLastClickTime = 0;
    private TextView info_text;

    //private boolean localDbFail = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate_activity);

        ratable = (ImageView) findViewById(R.id.ratable);
        rating = (SeekBar) findViewById(R.id.rating);
        numrating = (TextView) findViewById(R.id.numrating);
        nextButton = (Button) findViewById(R.id.next);
        skipButton = (Button) findViewById(R.id.skip);
        info_text = (TextView) findViewById(R.id.info_text);

        rating.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                if (fromUser) {
                    double d = progress / 10.0;
                    numrating.setText(String.valueOf(d));
                }

                //Toast.makeText(getApplicationContext(), String.valueOf(progress),Toast.LENGTH_SHORT).show();
            }
        });

        getParseUserCount();
    }


    public void getParseUserCount() {
        Toast.makeText(getApplicationContext(), "in getparsecount", Toast.LENGTH_SHORT).show();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserCount");
        query.getInBackground("yOiW7xb2Sv", new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "in getparsecount success", Toast.LENGTH_SHORT).show();
                    usernumber = object.getInt("count");
                    generateRand();
                } else {
                    Toast.makeText(getApplicationContext(), "rateActivity/getParseUserCount() " + e.toString() + "(User Count fetch failed)", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void generateRand() {
        Toast.makeText(getApplicationContext(), "in generateRand", Toast.LENGTH_SHORT).show();
        Random rand = new Random();
        //Toast.makeText(getApplicationContext(), String.valueOf(usernumber), Toast.LENGTH_SHORT).show();
        pointer = rand.nextInt(usernumber) + 1;
        getParseUserId();
    }

    public void getParseUserId() {
        Toast.makeText(getApplicationContext(), "in getParseUserId", Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplicationContext(), "got in", Toast.LENGTH_SHORT).show();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("usernumber", pointer);
        //Toast.makeText(getApplicationContext(), "got in in", Toast.LENGTH_SHORT).show();
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    ParseUser temp = objects.get(0);
                    //Toast.makeText(getApplicationContext(), temp.getUsername().toString(), Toast.LENGTH_SHORT).show();
                    userID = temp.get("userID").toString();
                    userName = temp.get("username").toString();
                    info_text.setText(userName);
                    info_text.append(", " + temp.get("Age"));
                    loadRatableLocal();
                    // The query was successful.
                } else {
                    Toast.makeText(getApplicationContext(), "rateActivity/getParseUserId() " + e.toString() + "(user info fetch failed)", Toast.LENGTH_SHORT).show();
                    // Something went wrong.
                }
            }
        });
    }

    public void loadRatableLocal() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.fromLocalDatastore();
        query.whereEqualTo("userID", userID);
        //Toast.makeText(getApplicationContext(), "got in in", Toast.LENGTH_SHORT).show();
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser object, ParseException e) {
                if (e == null) {
                    try {
                        ParseFile userImaageFile = object.getParseFile("ProfilePic");
                        userImaageFile.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    if (bmp != null) {
                                        Log.e("parse file ok", " null");
                                        // img.setImageBitmap(Bitmap.createScaledBitmap(bmp,
                                        // (display.getWidth() / 5),
                                        // (display.getWidth() /50), false));
                                        ratable.setImageBitmap(bmp);
                                        Toast.makeText(getApplicationContext(), "rateActivity/loadRatableLocal() (Loaded form Local) ", Toast.LENGTH_SHORT).show();
                                        nextButton.setVisibility(View.VISIBLE);
                                        skipButton.setVisibility(View.VISIBLE);
                                        loadNumRating();
                                        // img.setPadding(10, 10, 0, 0);
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "rateActivity/loadRatableLocal()  " + e.toString() + "(Loading form Local failed)", Toast.LENGTH_SHORT).show();
                                    loadRatableCloud();
                                    Log.e("paser after downloade", " null");
                                }

                            }
                        });
                    } catch (Exception error) {
                        Toast.makeText(getApplicationContext(), "rateActivity/loadRatableLocal() " + error.toString() + "(Exception, trying from cloud)", Toast.LENGTH_SHORT).show();
                        loadRatableCloud();
                        error.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "rateActivity/loadRatableLocal() " + e.toString() + "(maybe user row not there in local, trying cloud)", Toast.LENGTH_SHORT).show();
                    loadRatableCloud();
                    // Something went wrong.
                }
            }
        });
    }

    public void loadRatableCloud() {
        String url = "https://graph.facebook.com/" + userID + "/picture?type=large";
        Toast.makeText(getApplicationContext(), "rateActivity/loadRatableCloud() (loaded from cloud)", Toast.LENGTH_SHORT).show();
        GetXMLTask task = new GetXMLTask();
        task.execute(new String[]{url});
        loadNumRating();
    }

    public void loadNumRating() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Ratings");
        query.whereEqualTo("rated", userID);
        //Toast.makeText(getApplicationContext(),"got in"+ m.userID + ", " + userID,Toast.LENGTH_SHORT).show();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException error) {
                if (error == null) {
                    if (object != null) {
                        numrating.setText(object.getString("rating"));
                        try {
                            if (!object.getString("rating").equals("R"))
                                rating.setProgress((int) (Double.valueOf(object.getString("rating")) * 10));
                            else
                                rating.setProgress(50);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "rateActivity/loadRatableCloud() (error take a look)", Toast.LENGTH_SHORT).show();
                        }
                        numraters = object.getInt("numraters");
                        oldrating = object.getString("rating");
                        //if the show has been rated before

                    } else {
                        Toast.makeText(getApplicationContext(), "rateActivity/loadRatableCloud() (Rating object null)", Toast.LENGTH_SHORT).show();
                        //if the show has not been rated before
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "rateActivity/loadRatableCloud() ((New User!) how come present in Rating table!)", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                    numrating.setText("R");
                    rating.setProgress(50);
                }
            }
        });
    }

    private class GetXMLTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap map = null;
            for (String url : urls) {
                map = downloadImage(url);
            }
            return map;
        }

        // Sets the Bitmap returned by doInBackground
        @Override
        protected void onPostExecute(Bitmap result) {
            //result.createScaledBitmap(result, 100, 100, false);
            //welcomenote.append("Width : " + String.valueOf(result.getWidth()) + " Height : " + String.valueOf(result.getHeight()) + "Bytes : " + String.valueOf(result.getByteCount()));
            ratable.setImageBitmap(result);
            nextButton.setVisibility(View.VISIBLE);
            skipButton.setVisibility(View.VISIBLE);
        }

        // Creates Bitmap from InputStream and returns it
        private Bitmap downloadImage(String url) {
            Bitmap bitmap = null;
            InputStream stream = null;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;

            try {
                stream = getHttpConnection(url);
                bitmap = BitmapFactory.
                        decodeStream(stream, null, bmOptions);
                stream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bitmap;
        }

        // Makes HttpURLConnection and returns InputStream
        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
        }
    }




    //----------------------------------------------------------------------------------------------

    public void saveRating() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("RatingPairs");
        query.whereEqualTo("rater", m.userID);
        query.whereEqualTo("rated", userID);
        //Toast.makeText(getApplicationContext(),"got in"+ m.userID + ", " + userID,Toast.LENGTH_SHORT).show();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException error) {
                if (error == null) {
                    if (object != null) {
                        Toast.makeText(getApplicationContext(), "Already Rated!", Toast.LENGTH_SHORT).show();
                        //if the show has been rated before

                    } else {
                        Toast.makeText(getApplicationContext(), "Serious Exception Take a look!", Toast.LENGTH_SHORT).show();
                        //if the show has not been rated before
                    }
                } else {
                    Toast.makeText(getApplicationContext(), String.valueOf(error.getCode()), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                    Log.d("Error COde", String.valueOf(error.getCode()));
                    Toast.makeText(getApplicationContext(), String.valueOf(error.getCode()), Toast.LENGTH_SHORT).show();
                    if (error.getCode() == 101) {
                        Toast.makeText(getApplicationContext(), "New Rating Pair!", Toast.LENGTH_SHORT).show();
                        ParseObject tempobject = new ParseObject("RatingPairs");
                        tempobject.put("rater", m.userID);
                        tempobject.put("rated", userID);
                        tempobject.put("rating", String.valueOf(rating.getProgress() / 10.0));
                        tempobject.saveEventually();

                        //Toast.makeText(getApplicationContext(), "just there!", Toast.LENGTH_SHORT).show();
                        if (notrated) {
                            Toast.makeText(getApplicationContext(), "Was Not Yet Rated!", Toast.LENGTH_SHORT).show();
                            ParseObject temp1object = new ParseObject("Ratings");
                            temp1object.put("numraters", 1);
                            temp1object.put("rated", userID);
                            temp1object.put("rating", String.valueOf(rating.getProgress() / 10.0));
                            temp1object.saveEventually();
                        } else {
                            Toast.makeText(getApplicationContext(), "Rating Updating!", Toast.LENGTH_SHORT).show();
                            updateRating(); //todo
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Some other error code! (not 101)", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void updateRating() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Ratings");
        query.whereEqualTo("rated", userID);
        Toast.makeText(getApplicationContext(), "got in" + m.userID + ", " + userID, Toast.LENGTH_SHORT).show();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException error) {
                if (error == null) {
                    double temp = Double.valueOf(oldrating);
                    temp = (temp * numraters + Double.valueOf(numrating.getText().toString())) / (numraters + 1.0);
                    object.put("numraters", numraters + 1);
                    object.put("rated", userID);
                    object.put("rating", String.valueOf(temp));
                    Toast.makeText(getApplicationContext(), numraters + 1 + ", " + String.valueOf(temp), Toast.LENGTH_SHORT).show();
                    object.saveEventually();
                } else {
                    error.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Rating Update Failed Take a look!", Toast.LENGTH_SHORT).show();
                    //
                }
            }
        });
    }



    public void nextButtonClick(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            Toast.makeText(getApplicationContext(), "Simultaneous clicks Handled!", Toast.LENGTH_SHORT).show();
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        //Toast.makeText(getApplicationContext(),"nextInvisible",Toast.LENGTH_SHORT).show();
        nextButton.setVisibility(View.INVISIBLE);
        skipButton.setVisibility(View.INVISIBLE);
        saveRating();   //todo
        getParseUserCount();
    }

    public void skipButtonClick(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            Toast.makeText(getApplicationContext(), "Simultaneous clicks Handled!", Toast.LENGTH_SHORT).show();
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        nextButton.setVisibility(View.INVISIBLE);
        skipButton.setVisibility(View.INVISIBLE);
        getParseUserCount();
    }
}
