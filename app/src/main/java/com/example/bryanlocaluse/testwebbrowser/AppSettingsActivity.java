package com.example.bryanlocaluse.testwebbrowser;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class AppSettingsActivity extends Activity {

    public static class PrefFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);

        PrefFragment prefFragment = new PrefFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(android.R.id.content, prefFragment);
        fragmentTransaction.commit();


    }


}
