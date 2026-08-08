package com.example.wordsolverbot

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnOpenSettings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            WordSolverService.instance?.startSolving()
            updateStatus()
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            WordSolverService.instance?.stopSolving()
            updateStatus()
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val running = WordSolverService.instance != null
        val text = if (running) {
            "Status: accessibility service is ON. Tap Start, then switch to the game."
        } else {
            "Status: service not enabled yet. Tap 'Open Accessibility Settings' first."
        }
        findViewById<TextView>(R.id.statusText).text = text
    }
}
