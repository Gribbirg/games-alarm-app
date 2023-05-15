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
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.smartalarm.ui.viewmodels.MainActivityViewModel


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val navController = supportFragmentManager
            .findFragmentById(R.id.fragment)
            ?.findNavController()

        binding.bottomNavigationView.setupWithNavController(navController!!)

        val sharedPreference = getSharedPreferences("holiday_is_complete", Context.MODE_PRIVATE)

        val holidayIsComplete = sharedPreference.getBoolean("is_complete", false)

        if (viewModel.holidayAlertNeed(holidayIsComplete)) {

            AlertDialog.Builder(binding.root.context)
                .setTitle("Скоро праздник!")
                .setMessage("${viewModel.getHolidayText()} праздник, не забудьте изменить будильники")
                .setPositiveButton("Да") { dialog, _ -> dialog.dismiss()}
                .setNegativeButton("Больше не показывать") {dialog, _ ->
                    sharedPreference.edit().apply {
                        putBoolean("is_complete", true)
                        apply()
                    }
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }
}
