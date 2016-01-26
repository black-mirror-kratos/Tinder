package hue.recon.facebook;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class MainActivity extends Activity {
    private int oldcount;
    private int newcount;
    public static String userID;
    private String userName;
    private String gender;
    private String email;
    private String birthday;
    private Date bday;
    public static String age;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private TextView welcomenote;
    private ImageView profilepic;
    private TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(getApplicationContext(), "in onCreate", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        //Parse.enableLocalDatastore(this);
        //Parse.initialize(this);
        setContentView(R.layout.activity_main);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        welcomenote = (TextView) findViewById(R.id.welcomenote);
        profilepic = (ImageView) findViewById(R.id.profilepic);
        username = (TextView) findViewById(R.id.username);

        getParseUserCount();

        parseGetLocal();

        loginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday, user_friends"));
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //setContentView(R.layout.activity_second);
                welcomenote.setText(
                        "User ID: "
                                + loginResult.getAccessToken().getUserId()
                                + "\n" +
                                "Auth Token: "
                                + loginResult.getAccessToken().getToken()
                );

                userID = loginResult.getAccessToken().getUserId();
                parseStoreLocal();
                parseGetLocal();
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.d("LoginActivity", response.toString());
                                try {
                                    Log.d("TAG", object.toString());
                                    userName = object.getString("name");
                                    username.setText(object.getString("name"));
                                    gender = object.getString("gender");
                                    email = object.getString("email");
                                    try {
                                        birthday = object.getString("birthday");
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                                        try {
                                            bday = dateFormat.parse(birthday);
                                        } catch (java.text.ParseException e) {
                                            e.printStackTrace();
                                        }
                                        Calendar calendar = new GregorianCalendar();
                                        calendar.setTime(bday);
                                        age = String.valueOf(2016 - calendar.get(Calendar.YEAR));
                                    }
                                    catch (Exception e){
                                        age = "Not Shared";
                                    }
                                    Log.d("TAG", userID + ", " + userName + ", " + email + ", " + birthday);
                                    requestAsyncTask();
                                } catch (JSONException e) {
                                    //requestAsyncTask();
                                    e.printStackTrace();
                                }
                                // Application code
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email, birthday ,gender");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                welcomenote.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException e) {
                welcomenote.setText("Login attempt failed.");
            }
        });


//-------------------------------------------------------------------------------------------------
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "hue.recon.facebook",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }

//--------------------------------------------------------------------------------------------------
    }

    public void parseStoreLocal(){
        Toast.makeText(getApplicationContext(), "in parseStoreLocal()", Toast.LENGTH_SHORT).show();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("parseLocalUserID");
        query.fromLocalDatastore();
        query.whereEqualTo("marked", "marked");
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException error) {
                if (error == null) {
                    object.put("userID", userID);
                    object.put("marked", "marked");
                    Toast.makeText(getApplicationContext(), "UserID saved!", Toast.LENGTH_SHORT).show();
                    object.pinInBackground();
                } else {
                    ParseObject parseLocalUserID = new ParseObject("parseLocalUserID");
                    parseLocalUserID.put("marked", "marked");
                    parseLocalUserID.put("userID", userID);
                    parseLocalUserID.pinInBackground();
                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    //
                }
            }
        });
    }

    public void parseGetLocal(){
        Toast.makeText(getApplicationContext(), "in parseGetLocal()", Toast.LENGTH_SHORT).show();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("parseLocalUserID");
        query.fromLocalDatastore();
        query.whereEqualTo("marked", "marked");
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException error) {
                if (error == null) {
                    userID = object.getString("userID");

                    Thread locate = new Thread(){
                        public void run(){
                            try{
                                sleep(1000);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }finally{
                                Intent intent = new Intent(MainActivity.this,location_update.class);
                                startActivity(intent);
                            }
                        }
                    };
                    locate.start();

                    getImageParseLocal();
                    Toast.makeText(getApplicationContext(), "Local user ID Retrieved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "No Local user ID found!", Toast.LENGTH_SHORT).show();
                    //
                }
            }
        });
    }

    public void getImageParseLocal(){
        Toast.makeText(getApplicationContext(), "in getImageParseLocal()", Toast.LENGTH_SHORT).show();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("LocalUserData");
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException error) {
                if (error == null) {
                    ParseFile userImaageFile = object.getParseFile("file");
                    userName = object.get("username").toString();
                    username.setText(object.getString("username").toString());
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
                                    profilepic.setImageBitmap(bmp);
                                    // img.setPadding(10, 10, 0, 0);
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "error after geting data from localImage" + e.toString(), Toast.LENGTH_SHORT).show();
                                Log.e("paser after downloade", " null");
                            }

                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "No Local user ID found!" + error.toString(), Toast.LENGTH_SHORT).show();
                    //
                }
            }
        });
    }

    public void requestAsyncTask(){
        Toast.makeText(getApplicationContext(), "in requestAsyncTask()", Toast.LENGTH_SHORT).show();
        String url = "https://graph.facebook.com/" + userID + "/picture?type=large";
        Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
        GetXMLTask task = new GetXMLTask();
        task.execute(new String[]{url});
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void rateButtonClick(View view) {
        Intent intent = new Intent(this, rateActivity.class);
        startActivity(intent);
    }



    public void getParseUserCount(){
        Toast.makeText(getApplicationContext(), "in getParseUserCount()", Toast.LENGTH_SHORT).show();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserCount");
        query.getInBackground("yOiW7xb2Sv", new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    oldcount = object.getInt("count");
                    //FbRegisterCallback();
                } else {
                    //
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
            Toast.makeText(getApplicationContext(), "in onPostExecute(Bitmap result)", Toast.LENGTH_SHORT).show();
            //result.createScaledBitmap(result, 100, 100, false);
            welcomenote.append("Width : " + String.valueOf(result.getWidth()) + " Height : " + String.valueOf(result.getHeight()) + "Bytes : " + String.valueOf(result.getByteCount()));
            profilepic.setImageBitmap(result);
            newParseUserSignup(result);
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

    public void storeImageParseLocalAndCloud(Bitmap result, ParseUser user){
        Toast.makeText(getApplicationContext(), "in storeImageParseLocalAndCloud(Bitmap result, ParseUser user)", Toast.LENGTH_SHORT).show();
        emptyLocalParse();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] data = stream.toByteArray();
        ParseFile file = new ParseFile("user.png",data);
        try {
            file.save();
        } catch (ParseException e) {
            Log.d("save file error", e.toString());
            e.printStackTrace();
        }
        ParseObject object = new ParseObject("LocalUserData");
        object.put("file", file);
        object.put("username", userName);
        try {
            object.pin();
        } catch (ParseException e) {
            Toast.makeText(getApplicationContext(),"pinning image error",Toast.LENGTH_SHORT).show();
            Log.d("pinning image error", e.toString());
            e.printStackTrace();
        }
        user.put("ProfilePic", file);

    }

    public void emptyLocalParse(){
        Toast.makeText(getApplicationContext(), "in emptyLocalParse()", Toast.LENGTH_SHORT).show();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("LocalUserData");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objectList, ParseException error) {
                if (error == null) {
                    ParseObject.unpinAllInBackground(objectList);
                    Toast.makeText(getApplicationContext(), "unpinInBackground success!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "unpinInBackground error!" + error.toString(), Toast.LENGTH_SHORT).show();
                    //
                }
            }
        });
    }

    public void newParseUserSignup(Bitmap result){
        Toast.makeText(getApplicationContext(), "in newParseUserSignup(Bitmap result)", Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(),"SignUp!",Toast.LENGTH_SHORT).show();
        final ParseUser user = new ParseUser();
        storeImageParseLocalAndCloud(result, user);
        Toast.makeText(getApplicationContext(), userID, Toast.LENGTH_SHORT).show();
        user.put("userID", userID);
        user.setUsername(userName);
        user.setPassword(userID);
        user.setEmail(email);
        user.put("Gender", gender);
        user.put("Age",age);
        user.put("usernumber", oldcount + 1);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    incrementParseUserCount();
                    setupUserTracking();
                    Toast.makeText(getApplicationContext(), "no fault", Toast.LENGTH_SHORT).show();
                    getDbCloudToLocal();
                    // Hooray! Let them use the app now.
                } else {
                    Toast.makeText(getApplicationContext(), "Already Registered!", Toast.LENGTH_SHORT).show();
                    getDbCloudToLocal();
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                }
            }
        });
    }

    public void getDbCloudToLocal(){
        Toast.makeText(getApplicationContext(), "in getDbCloudToLocal()", Toast.LENGTH_SHORT).show();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        //Toast.makeText(getApplicationContext(), "got in in", Toast.LENGTH_SHORT).show();
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    ParseObject.pinAllInBackground(objects);
                } else {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    // Something went wrong.
                }
            }
        });
    }

    public void setupUserTracking(){
        Toast.makeText(getApplicationContext(), "in setupUserTracking()", Toast.LENGTH_SHORT).show();
        ParseObject parseLocation = new ParseObject("Location");
        parseLocation.put("user", userID);
        parseLocation.saveEventually();
    }

    public void incrementParseUserCount(){
        Toast.makeText(getApplicationContext(), "in incrementParseUserCount()", Toast.LENGTH_SHORT).show();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserCount");
        query.getInBackground("yOiW7xb2Sv", new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    //oldcount = object.getInt("count");
                    object.put("count", oldcount + 1);
                    object.saveEventually();
                } else {
                    //
                }
            }
        });
    }

    public void locateOnClick(View view){
        Intent intent = new Intent(this,locate_activity.class);
        startActivity(intent);
    }


}
