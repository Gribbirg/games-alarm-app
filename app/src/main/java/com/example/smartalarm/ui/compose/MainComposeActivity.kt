package com.example.smartalarm.ui.compose

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.smartalarm.R
import com.example.smartalarm.databinding.ActivityMainBinding
import com.example.smartalarm.ui.compose.addalarm.AddAlarmViewModel
import com.example.smartalarm.ui.compose.alarms.AlarmsViewModel
import com.example.smartalarm.ui.compose.navigation.BottomNavigationBar
import com.example.smartalarm.ui.compose.navigation.NavGraph
import com.example.smartalarm.ui.compose.profile.ProfileViewModel
import com.example.smartalarm.ui.compose.records.RecordsViewModel
import com.example.smartalarm.ui.compose.settings.SettingsViewModel
import com.example.smartalarm.ui.theme.GamesAlarmTheme
import com.example.smartalarm.ui.viewmodels.MainActivityViewModel

class MainComposeActivity : ComponentActivity() {

    private val notificationRequestCode = 100
    private val vibrationRequestCode = 101
    private val readExternalStorageRequestCode = 102
    private val readMediaAudioRequestCode = 103

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(
                notificationRequestCode,
                android.Manifest.permission.POST_NOTIFICATIONS,
                "показ уведомлений"
            )

            checkPermission(
                readMediaAudioRequestCode,
                android.Manifest.permission.READ_MEDIA_AUDIO,
                "чтение аудио-файлов для выбора мелодии"
            )
        }

        checkPermission(
            vibrationRequestCode,
            android.Manifest.permission.VIBRATE,
            "вызов вибрации"
        )

        checkPermission(
            readExternalStorageRequestCode,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            "чтение файлов для выбора мелодии"
        )

        val sharedPreference = getSharedPreferences("holiday_is_complete", Context.MODE_PRIVATE)

        val holidayIsComplete = sharedPreference.getBoolean("is_complete", false)
        Log.i("grib", holidayIsComplete.toString())

        if (viewModel.holidayAlertNeed(holidayIsComplete)) {

            AlertDialog.Builder(applicationContext)
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

        setContent {
            GamesAlarmTheme {
                GamesAlarmApp()
            }
        }
    }

    @Composable
    fun GamesAlarmApp() {
        val alarmsViewmodel: AlarmsViewModel by viewModels()
        val addAlarmViewModel: AddAlarmViewModel by viewModels()
        val profileViewModel: ProfileViewModel by viewModels()
        val recordsViewModel: RecordsViewModel by viewModels()
        val settingsViewModel: SettingsViewModel by viewModels()

        val navController = rememberNavController()

        Scaffold(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            bottomBar = { BottomNavigationBar(navController = navController) }
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                NavGraph(
                    navHostController = navController,
                    alarmsViewModel = alarmsViewmodel,
                    recordsViewModel = recordsViewModel,
                    profileViewModel = profileViewModel,
                    settingsViewModel = settingsViewModel,
                    addAlarmViewModel = addAlarmViewModel
                )
            }
        }
    }

    private fun checkPermission(requestCode: Int, permission: String, name: String) {
        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
            }

            shouldShowRequestPermissionRationale(permission) -> {

                val builder = AlertDialog.Builder(this)
                builder.apply {
                    setMessage("Для корректной работы приложения необходимо разрешение на $name.")
                    setTitle("Необходимо разрешение")
                    setPositiveButton("Разрешить") { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this@MainComposeActivity,
                            arrayOf(permission),
                            requestCode
                        )
                    }
                }

                val dialog = builder.create()
                dialog.show()
            }

            else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }
}