package com.example.waffledefender.emotiondetectormobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_settings);
       // initSettingsList();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.home_selection, menu);
        return true;
    }
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item){
//        int id = item.getItemId();
//
//        if(id == R.id.action_settings){
//            SharedPreferences.Editor editPref = getSharedPreferences("SettingsPreferences", 0).edit();
//
//            Preference petName = findPreference("pref_key_dog_name");
//            EditTextPreference editPetName = (EditTextPreference) petName;
//            editPref.putString("petName", editPetName.getText().toString()).commit();
//
//            Preference petAge = findPreference("pref_key_dog_age");
//            EditTextPreference editPetAge = (EditTextPreference) petAge;
//
//            for(Character c : editPetAge.getText().toCharArray()){
//                if(Character.isLetter(c)){
//                    Toast.makeText(this, "Age cannot contain letters, only digits. Defaulting to '42'.", Toast.LENGTH_SHORT).show();
//                    editPetAge.setText("42");
//                }
//            }
//
//            editPref.putString("petAge", editPetAge.getText().toString()).commit();
//
//            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onResume(){
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        checkPreference();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        checkPreference();
    }

    private void checkPreference(){
        SharedPreferences.Editor editPref = getSharedPreferences("SettingsPreferences", 0).edit();

        Preference petName = findPreference("pref_key_dog_name");
        EditTextPreference editPetName = (EditTextPreference) petName;
        editPref.putString("petName", editPetName.getText().toString()).commit();

        Preference petAge = findPreference("pref_key_dog_age");
        EditTextPreference editPetAge = (EditTextPreference) petAge;

        for(Character c : editPetAge.getText().toCharArray()){
            if(Character.isLetter(c)){
                Toast.makeText(this, "Age cannot contain letters, only digits. Defaulting to '42'.", Toast.LENGTH_SHORT).show();
                editPetAge.setText("42");
            }
        }
    }
}
