package com.example.smartalarm.ui.viewmodels

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartalarm.data.data.ArifData

class CalcGameViewModel(application: Application) : AndroidViewModel(application) {
    private val timeStarted = System.currentTimeMillis()
    private var timeCurrent: Long = 0

    var timeCurrentString: MutableLiveData<String> = MutableLiveData()

    private val handler = Handler(Looper.getMainLooper())
    private var runnable = object : Runnable {
        override fun run() {
            timeCurrent = System.currentTimeMillis() - timeStarted
            timeCurrentString.postValue("${timeCurrent / 60000}.${(timeCurrent / 1000) % 60}")
            handler.postDelayed(this, 1000)
        }
    }

    lateinit var arifData: ArifData

    private var score = 0
    var difficulty = 0

    init {
        handler.post(runnable)
    }

    fun setDifficultyLevel(difficulty: Int) {
        this.difficulty = difficulty
    }

    fun finishScore(): Int {
        score += ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
        return score
    }

    fun mistake() {
        score -= 10
    }

    fun generateRandom() {
        arifData = when(difficulty) {
            1 ->  ArifData(
                arrayListOf(
                    (1..10).random(),
                    (1..100).random()
                ),
                arrayListOf(
                    (2..10).random(),
                    (2..10).random()
                )
            )
            2 -> ArifData(
                arrayListOf(
                    (1..100).random(),
                    (1..100).random()
                ),
                arrayListOf(
                    (2..9).random(),
                    (11..99).random()
                )
            )
            3 -> ArifData(
                arrayListOf(
                    (1..100).random(),
                    (1..100).random(),
                    (1..100).random()
                ),
                arrayListOf(
                    (2..9).random(),
                    (2..9).random(),
                    (2..9).random()
                )
            )
            else -> ArifData(arrayListOf(), arrayListOf())
        }
    }
}