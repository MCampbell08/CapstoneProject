package com.example.waffledefender.emotiondetectormobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        if(v.getId() == R.id.refreshButton){
            Connection conn = getRemoteConnection();
        }
    }
    private static Connection getRemoteConnection() {
        if (System.getenv("heartbeatdata.cvqgs9wo2qak.us-west-1.rds.amazonaws.com") != null) {

            try {
                Class.forName("org.postgresql.Driver");
                String dbName = System.getenv("heartbeatdata");
                String userName = System.getenv("waffledefender");
                String password = System.getenv("1_Tails_4");
                String hostname = System.getenv("heartbeatdata.cvqgs9wo2qak.us-west-1.rds.amazonaws.com");
                String port = System.getenv("3306");
                String jdbcUrl = "jdbc:postgresql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
                Connection con = DriverManager.getConnection(jdbcUrl);
                return con;
            }
            catch (ClassNotFoundException e) { Log.w(e.toString(), e.toString());}
            catch (SQLException e) { Log.w(e.toString(),e.toString());}
        }
        return null;
    }
}