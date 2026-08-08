package com.example.wordsolverbot

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnOpenSettings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            val service = WordSolverService.instance
            if (service != null) {
                service.startSolving()
                Toast.makeText(this, "Bot Started! Ab Game open karein.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error: Service OFF hai! Pehle Accessibility ON karein.", Toast.LENGTH_LONG).show()
            }
            updateStatus()
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            val service = WordSolverService.instance
            if (service != null) {
                service.stopSolving()
                Toast.makeText(this, "Bot Stopped!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Service active nahi hai.", Toast.LENGTH_SHORT).show()
            }
            updateStatus()
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val isConnected = WordSolverService.instance != null
        val text = if (isConnected) {
            "Status: Accessibility Service ACTIVE hai ✅\n'Start' dabayein aur game open karein."
        } else {
            "Status: Service Active nahi hai ❌\nPehle 'Open Accessibility Settings' par click karke Permission ON karein."
        }
        findViewById<TextView>(R.id.statusText).text = text
    }
}
