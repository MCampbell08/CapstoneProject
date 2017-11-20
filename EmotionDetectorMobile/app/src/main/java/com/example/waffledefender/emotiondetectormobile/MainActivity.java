package com.example.waffledefender.emotiondetectormobile;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Connection connection = null;

    private static String dbName = "heartbeatdata";
    private static String userName = "waffledefender";
    private static String password = "1_Tails_4";
    private static String hostname = "heartbeatdata.cvqgs9wo2qak.us-west-1.rds.amazonaws.com";
    private static String port = "3306";

    private static EmotionTranslate translate = null;
    private static ArrayList<String> heartRates = null;

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int PIC_CROP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPreferences();
        setOnClickListeners();
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

    private void displayHeartbeat(){
        if(connection != null) {
            try {
                heartRates = new ArrayList<>();
                Statement statement = connection.createStatement();

                String heartbeat = null;
                ResultSet resultSet = statement.executeQuery("select * from heartbeat");
                while(resultSet.next()) {
                    heartbeat = resultSet.getString("HeartbeatValue");
                    heartRates.add(heartbeat);
                }
                translate.setCurrentHeartbeat(heartRates.get(0));
                String emotion = translate.translate().toString();

                TextView heartbeatTextView = (TextView) findViewById(R.id.heartrate);
                TextView emotionTextView = (TextView) findViewById(R.id.currentEmotion);

                heartbeatTextView.setText(heartbeat);
                emotionTextView.setText(emotion);

                SharedPreferences.Editor mEditor = getSharedPreferences("Preferences", 0).edit();

                mEditor.putString("heartbeatVal", heartbeatTextView.getText().toString()).commit();
                mEditor.putString("heartbeatEmotion", emotion).commit();

                resultSet = statement.executeQuery("select * from heartbeat");
                resultSet.next();

                Toast.makeText(this, "Updated as of: " + resultSet.getString("TimeOfRecord"), Toast.LENGTH_SHORT).show();

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
            Uri imageURI = Uri.parse(preferences.getString("profileUri", ""));

            TextView heartbeatTextView = (TextView) findViewById(R.id.heartrate);
            heartbeatTextView.setText(beat);

            TextView emotionTextView = (TextView) findViewById(R.id.currentEmotion);
            emotionTextView.setText(emotion);

            ImageView imageView = (ImageView) findViewById(R.id.accountIcon);
            imageView.setImageURI(imageURI);

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        try{
            if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null){
                Uri selectedImage = data.getData();
                performCropping(selectedImage);

                SharedPreferences.Editor mEditor = getSharedPreferences("Preferences", 0).edit();

                mEditor.putString("profileUri", selectedImage.toString()).commit();
            }
            else if(requestCode == PIC_CROP){
                Bundle extras = data.getExtras();
                Bitmap thePic = extras.getParcelable("data");

                ImageView imageView = (ImageView)findViewById(R.id.accountIcon);
                imageView.setImageBitmap(thePic);
            }
            else{
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){
            Toast.makeText(this, "Can not use this image, please try again or select another!", Toast.LENGTH_SHORT).show();
        }
    }
    private void performCropping(Uri picUri){
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch(ActivityNotFoundException e){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}