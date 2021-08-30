package com.manichord.mgit.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commit
import com.manichord.mgit.ui.fragments.SettingsFragment
import com.manichord.mgitt.R

class UserSettingsActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings2)
        val toolbar = findViewById<Toolbar>(R.id.toolbar3)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        supportFragmentManager.commit {
           replace(R.id.frameLayout,SettingsFragment())
        }

    }
}
