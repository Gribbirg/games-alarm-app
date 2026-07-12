package com.example.smartalarm.feature.games.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Единственная активити демо-приложения игр: хостит навигацию
 * «список игр → игра → результат».
 */
class DemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)
    }
}
