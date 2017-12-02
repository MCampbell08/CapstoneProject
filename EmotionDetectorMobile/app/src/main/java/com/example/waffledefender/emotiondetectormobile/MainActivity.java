package com.example.waffledefender.emotiondetectormobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Observer {

    private Connection connection = null;

    private static String dbName = "heartbeatdata";
    private static String userName = "waffledefender";
    private static String password = "1_Tails_4";
    private static String hostname = "heartbeatdata.cvqgs9wo2qak.us-west-1.rds.amazonaws.com";
    private static String port = "3306";

    private static boolean heartbeatAnimate = false;

    private static EmotionTranslate translate = null;

    private static ArrayList<String> heartRateValues = null;
    private static ArrayList<String> heartRateTimeStamps = null;

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int PIC_CROP = 2;

    private static Observable observable = new Observable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPreferences();
        setOnClickListeners();
        ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.mainLayout);
        layout.setOnTouchListener(new SwipingListener(this));
        new HeartbeatAnimation().execute("");
        observable.addObserver(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.settings_selection, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_settings){
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.refreshButton) {
            getRemoteConnection();
            displayHeartbeat();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        try{
            if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null){
                Uri selectedImage = data.getData();
                performCropping(selectedImage);
            }
            else if(requestCode == PIC_CROP){
                Bundle extras = data.getExtras();
                Bitmap thePic = extras.getParcelable("data");

                ImageView imageView = (ImageView)findViewById(R.id.accountIcon);
                imageView.setImageBitmap(thePic);

                imageView.buildDrawingCache();
                Bitmap bitmap = imageView.getDrawingCache();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                byte[] imageBytes = stream.toByteArray();

                String imgString = Base64.encodeToString(imageBytes, 0);

                SharedPreferences.Editor mEditor = getSharedPreferences("Preferences", 0).edit();

                mEditor.putString("profileStream", imgString).commit();
            }
            else{
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){
            Toast.makeText(this, "Can not use this image, please try again or select another!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void update(Observable observable, Object o) {

    }

    private void displayHeartbeat(){
        if(connection != null) {
            try {
                heartRateValues = new ArrayList<>();
                heartRateTimeStamps = new ArrayList<>();

                Statement statement = connection.createStatement();

                String heartbeatValue = null;
                String heartbeatTimeStamp = null;

                ResultSet resultSet = statement.executeQuery("select * from heartbeat");
                while(resultSet.next()) {
                    heartbeatValue = resultSet.getString("HeartbeatValue");
                    heartbeatTimeStamp = resultSet.getString("TimeOfRecord");

                    heartRateValues.add(heartbeatValue);
                    heartRateTimeStamps.add(heartbeatTimeStamp);
                }
                translate.setCurrentHeartbeat(heartRateValues.get(0));
                String emotion = translate.translate().toString();

                TextView heartbeatTextView = (TextView) findViewById(R.id.heartrate);
                TextView emotionTextView = (TextView) findViewById(R.id.currentEmotion);

                heartbeatTextView.setText(heartbeatValue);
                emotionTextView.setText(emotion);

                Set<String> heartRateValSet = new LinkedHashSet<>();
                Set<String> heartRateTimeSet = new LinkedHashSet<>();

                for (int i = 0; i < heartRateValues.size(); i++) {
                    heartRateValSet.add(heartRateValues.get(i) + "/" + i);
                    heartRateTimeSet.add(heartRateTimeStamps.get(i) + "/" + i);
                }

                SharedPreferences.Editor mEditor = getSharedPreferences("Preferences", 0).edit();

                mEditor.putString("heartbeatVal", heartbeatTextView.getText().toString()).commit();
                mEditor.putString("heartbeatEmotion", emotion).commit();
                mEditor.putStringSet("heartbeatValues", heartRateValSet).commit();
                mEditor.putStringSet("heartbeatTimeStamps", heartRateTimeSet).commit();

                resultSet = statement.executeQuery("select * from heartbeat");
                resultSet.next();

                Date date  = new Date();

                Timestamp ts = Timestamp.valueOf(resultSet.getString("TimeOfRecord"));
                date.setTime(ts.getTime());

                String timeStampFormatted = new SimpleDateFormat("yyyy").format(date);

                if(Double.parseDouble(new SimpleDateFormat("yyyy").format(new Date())) != Double.parseDouble(timeStampFormatted)){
                    timeStampFormatted = new SimpleDateFormat("MMM dd, yyyy").format(date);
                }
                else {
                    timeStampFormatted = new SimpleDateFormat("MMM dd").format(date);
                }

                Toast.makeText(this, "Updated as of: " + timeStampFormatted, Toast.LENGTH_SHORT).show();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void getRemoteConnection(){
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;

        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initPreferences(){
        String heartbeatNum = "00";
        SharedPreferences preferences = getSharedPreferences("Preferences", 0);
        if(preferences.contains("heartbeatVal")){

            String beat = preferences.getString("heartbeatVal", "00");
            String emotion = preferences.getString("heartbeatEmotion", "Meh");
            String imageStream = preferences.getString("profileStream", "");

            TextView heartbeatTextView = (TextView) findViewById(R.id.heartrate);
            heartbeatTextView.setText(beat);

            TextView emotionTextView = (TextView) findViewById(R.id.currentEmotion);
            emotionTextView.setText(emotion);

            ImageView imageView = (ImageView) findViewById(R.id.accountIcon);

            if(!imageStream.equals("")) {
                String base = imageStream;
                byte[] imageBytes = Base64.decode(imageStream.getBytes(),Base64.DEFAULT);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
            }
            else{
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.account));
            }

            heartbeatNum = beat;
        }
        translate = new EmotionTranslate(heartbeatNum);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void setOnClickListeners(){
        ImageView avatarIcon = (ImageView)findViewById(R.id.accountIcon);

        avatarIcon.setClickable(true);
        avatarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });
    }

    private void performCropping(Uri picUri){
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(picUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 456);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch(ActivityNotFoundException e){
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public void setHeartbeatAnimate(boolean bool){
        heartbeatAnimate = bool;
        observable.notifyObservers();
    }

    public static boolean getHeartbeatAnimate(){
        return heartbeatAnimate;
    }

    public class HeartbeatAnimation extends AsyncTask<String, Void, String> {

        private AnimationDrawable animationDrawable = null;

        @Override
        protected String doInBackground(String... strings) {
            while (!MainActivity.getHeartbeatAnimate()) {
                if (MainActivity.getHeartbeatAnimate()) {
                    final ImageView heartImage = (ImageView) findViewById(R.id.heartIcon);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            heartImage.setImageResource(R.drawable.heartbeat_animation);
                            heartImage.setBackgroundResource(R.drawable.heartbeat_animation);
                            animationDrawable = (AnimationDrawable) heartImage.getBackground();
                        }
                    });
                    animationDrawable.start();
                }
            }
            return "Animation Done";
        }
    }
    public class ObservedObject extends Observable {
        private Boolean watchedValue;

        public ObservedObject(Boolean value) {
            watchedValue = value;
        }

        public void setValue(Boolean value) {

            if(!watchedValue.equals(value)) {
                watchedValue = value;

                setChanged();
            }

        }
    }
//    public class ObservableDemo implements Observer {
//        public String name;
//        public ObservableDemo(String name) {
//            this.name = name;
//        }
//
//        public static void main(String[] args) {
//            // create watched and watcher objects
//            ObservedObject watched = new ObservedObject("Original Value");
//            // watcher object listens to object change
//            ObservableDemo watcher = new ObservableDemo("Watcher");
//            // add observer to the watched object
//            watched.addObserver(watcher);
//
//            // trigger value change
//            System.out.println("setValue method called...");
//            watched.setValue("New Value");
//            // check if value has changed
//            if(watched.hasChanged())
//                System.out.println("Value changed");
//            else
//                System.out.println("Value not changed");
//        }
//
//        public void update(Observable obj, Object arg) {
//            System.out.println("Update called");
//        }
//    }
}