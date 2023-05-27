package com.example.smartalarm.ui.activities

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.example.smartalarm.databinding.ActivityGamesBinding
import com.google.android.material.color.MaterialColors

class GamesActivity : AppCompatActivity() {
    lateinit var binding: ActivityGamesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamesBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }
}