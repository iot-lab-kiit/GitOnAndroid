package com.manichord.mgit.ui.fragments;

import android.content.Context;
//import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
//import androidx.core.app.TaskStackBuilder;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

//import com.manichord.mgit.utils.BasicFunctions;
import com.manichord.mgitt.R;
//import com.manichord.mgit.ui.RepoListActivity;

public class SettingsFragment extends PreferenceFragmentCompat {
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // need to set as for historical reasons SGit uses custom prefs file
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(getString(R.string.preference_file_key));
        prefMgr.setSharedPreferencesMode(Context.MODE_PRIVATE);

        // Load the preferences from an XML resource
        if(savedInstanceState != null) { // prevent to load preferences multiple times to Fragment
            addPreferencesFromResource(R.xml.preferences);
        }

        final String themePrefKey = getString(R.string.pref_key_use_theme_id);
        final String gravatarPrefKey = getString(R.string.pref_key_use_gravatar);
        final String useEnglishPrefKey = getString(R.string.pref_key_use_english);
        final String userNamePrefKey = getString(R.string.pref_key_git_user_name);
        final String userEmailPrefKey = getString(R.string.pref_key_git_user_email);

        ListPreference themePref = findPreference(themePrefKey);
        SwitchPreferenceCompat gravatarPref = findPreference(gravatarPrefKey);
        SwitchPreferenceCompat useEnglishPref = findPreference(useEnglishPrefKey);
        EditTextPreference userNamePref =  findPreference(userNamePrefKey);
        EditTextPreference userEmailPref = findPreference(userEmailPrefKey);

        mListener = (sharedPreferences, key) -> {
            /*
            if (themePrefKey.equals(key) || useEnglishPrefKey.equals(key)) {
                // nice trick to recreate the back stack, to ensure existing activities onCreate() are
                // called to set new theme, courtesy of: http://stackoverflow.com/a/28799124/85472
                TaskStackBuilder.create(requireActivity())
                        .addNextIntent(new Intent(getActivity(), RepoListActivity.class))
                        .addNextIntent(requireActivity().getIntent())
                        .startActivities();
            }
            else if (gravatarPrefKey.equals(key)) {
                BasicFunctions.getImageLoader().clearMemoryCache();
                BasicFunctions.getImageLoader().clearDiskCache();
            }
            */

            // Attach a listener to update fields
            if (themePref != null){
                themePref.setValueIndex
                    (Integer.parseInt(sharedPreferences.getString(themePrefKey, "")));
            }
            if (gravatarPref != null ){
                gravatarPref.setChecked(sharedPreferences.getBoolean(gravatarPrefKey, true));
            }
            if (useEnglishPref != null){
                useEnglishPref.setChecked(sharedPreferences.getBoolean(useEnglishPrefKey, false));
            }
            if (userNamePref != null) {
                userNamePref.setSummary(sharedPreferences.getString(userNamePrefKey, ""));
            }
            if (userEmailPref != null) {
                userEmailPref.setSummary(sharedPreferences.getString(userEmailPrefKey, ""));
            }
        };

        // Invoke callback manually to display the current values
        mListener.onSharedPreferenceChanged(prefMgr.getSharedPreferences(), userNamePrefKey);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mListener);
    }
}
