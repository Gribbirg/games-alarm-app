package com.example.smartalarm.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.smartalarm.R
import com.example.smartalarm.databinding.ActivityMainBinding
import com.example.smartalarm.ui.fragments.AlarmsFragment
import com.example.smartalarm.ui.fragments.ProfileFragment
import com.example.smartalarm.ui.fragments.RecordsFragment
import com.example.smartalarm.ui.fragments.SettingsFragment
import android.app.AlarmManager


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val navController = supportFragmentManager
            .findFragmentById(R.id.fragment)
            ?.findNavController()

//        val appBarConfiguration = AppBarConfiguration(setOf(
//            R.id.alarmsFragment,
//            R.id.recordsFragment,
//            R.id.profileFragment,
//            R.id.settingsFragment
//        ))
//
//        setupActionBarWithNavController(navController!!, appBarConfiguration)

        binding.bottomNavigationView.setupWithNavController(navController!!)
    }
}
