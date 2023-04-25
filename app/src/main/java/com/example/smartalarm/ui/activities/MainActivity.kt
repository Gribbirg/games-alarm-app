package com.example.smartalarm.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import com.example.smartalarm.R
import com.example.smartalarm.databinding.ActivityMainBinding
import com.example.smartalarm.ui.fragments.AlarmsFragment
import com.example.smartalarm.ui.fragments.ProfileFragment
import com.example.smartalarm.ui.fragments.RecordsFragment
import com.example.smartalarm.ui.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val alarmsFragment = AlarmsFragment()
        val recordsFragment = RecordsFragment()
        val profileFragment = ProfileFragment()
        val settingsFragment = SettingsFragment()

        setCurrentFragment(alarmsFragment)

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.alarms -> setCurrentFragment(alarmsFragment)
                R.id.records -> setCurrentFragment(recordsFragment)
                R.id.profile -> setCurrentFragment(profileFragment)
                R.id.settings -> setCurrentFragment(settingsFragment)
                else -> {}
            }
            true
        }


    }

    fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_container, fragment)
            commit()
        }
}