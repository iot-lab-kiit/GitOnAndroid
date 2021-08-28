package com.manichord.mgit.activities;

import android.os.Bundle;

import com.manichord.mgit.android.activities.SheimiFragmentActivity;
import com.manichord.mgit.fragments.SettingsFragment;

/**
 * Activity for user settings
 */
public class UserSettingsActivity extends SheimiFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
