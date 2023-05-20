package com.example.smartalarm.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.db.RecordsData
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.ui.viewmodels.MainActivityViewModel
import kotlinx.coroutines.launch


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
                .setPositiveButton("Ок") { dialog, _ -> dialog.dismiss() }
                .setNegativeButton("Больше не показывать") { dialog, _ ->
                    sharedPreference.edit().apply {
                        putBoolean("is_complete", true)
                        apply()
                    }
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        if (viewModel.resetAlertNeed()) {
            sharedPreference.edit().apply {
                remove("is_complete")
                apply()
            }
        }

//        lifecycleScope.launch {
//            val rep = AlarmDbRepository(
//                AlarmsDB.getInstance(applicationContext)?.alarmsDao()!!
//            )
//            for (i in 0..200) {
//                rep.insertRecord(
//                    RecordsData(
//                        gameId = i % 3 + 1,
//                        gameName = "Тестовая игра 1",
//                        date = "01.02.$i",
//                        recordScore = i,
//                        recordTime = "05.20"
//                    )
//                )
//            }
//        }
    }
}
