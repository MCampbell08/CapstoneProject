package com.example.waffledefender.emotiondetectormobile;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Connection connection = null;
    private static String dbName = "heartbeatdata";
    private static String userName = "waffledefender";
    private static String password = "1_Tails_4";
    private static String hostname = "heartbeatdata.cvqgs9wo2qak.us-west-1.rds.amazonaws.com";
    private static String port = "3306";
    private static EmotionTranslate translate = null;

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
                Statement statement = connection.createStatement();

                ResultSet resultSet = statement.executeQuery("select * from heartbeat");
                resultSet.next();

                String heartbeat = resultSet.getString("HeartbeatValue");
                translate.setCurrentHeartbeat(heartbeat);
                String emotion = translate.translate().toString();

                TextView heartbeatTextView = (TextView) findViewById(R.id.heartrate);
                TextView emotionTextView = (TextView) findViewById(R.id.currentEmotion);

                heartbeatTextView.setText(heartbeat);
                emotionTextView.setText(emotion);

                SharedPreferences.Editor mEditor = getSharedPreferences("Preferences", 0).edit();
                mEditor.putString("heartbeatVal", heartbeatTextView.getText().toString()).commit();
                mEditor.putString("heartbeatEmotion", emotion).commit();
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

            TextView heartbeatTextView = (TextView) findViewById(R.id.heartrate);
            heartbeatTextView.setText(beat);

            TextView emotionTextView = (TextView) findViewById(R.id.currentEmotion);
            emotionTextView.setText(emotion);
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
                startActivityForResult(intent, RESULT_OK);
            }
        });
    }
}