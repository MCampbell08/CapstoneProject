package com.example.waffledefender.emotiondetectormobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initSettingsList();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.home_selection, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_settings){
            SharedPreferences.Editor editPref = getSharedPreferences("SettingsPreferences", 0).edit();

            TextView petName = (TextView)findViewById(R.id.nameEditable);
            editPref.putString("petName", petName.getText().toString()).commit();

            TextView petAge = (TextView)findViewById(R.id.ageEditable);
            editPref.putString("petAge", petAge.getText().toString()).commit();

            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void initSettingsList(){
        SharedPreferences preferences = getSharedPreferences("SettingsPreferences", 0);

        if(preferences != null){
            String petName = preferences.getString("petName", "Fido");

            TextView petNameTextView = (TextView) findViewById(R.id.nameEditable);
            petNameTextView.setText(petName);

            String petAge = preferences.getString("petAge", "42");

            TextView petAgeTextView = (TextView) findViewById(R.id.ageEditable);
            petAgeTextView.setText(petAge);
        }
    }
}
