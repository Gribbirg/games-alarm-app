package com.example.smartalarm.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smartalarm.databinding.ActivityGamesBinding

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