package com.aceplus.steptracker

import android.content.*
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    companion object {
        private const val ACTIVITY_RECOGNITION = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (requiredPermission()) {
            requestPermissions(arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), ACTIVITY_RECOGNITION)
        }

        checkSensor()
        val serviceIntent = Intent(this, StepCountingService::class.java)
        startTracking(serviceIntent)

    }

    private fun startTracking(service: Intent) {
        if (requiredPermission())
            showStatus("Permission Denied", true)
        else
            ContextCompat.startForegroundService(this, service)
    }

    private fun stopTracking(service: Intent) {
        stopService(service)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            intent?.let {
                updateData(it)
            }
        }
    }

    private fun updateData(intent: Intent) {

        val countResult = intent.getIntExtra(StepCountingService.COUNT_RESULT, 0)
        val detectResult = intent.getIntExtra(StepCountingService.DETECT_RESULT, 0)

        count.text = countResult.toString()
        detection.text = detectResult.toString()

    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, IntentFilter(StepCountingService.BROADCAST_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }

    private fun checkSensor() {

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER))
            showStatus("Step Counter Sensor Detected")
        else
            showStatus("Step Counter Sensor Not Detected")

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR))
            showStatus("Step Detector Sensor Detected")
        else
            showStatus("Step Detector Sensor Not Detected")

    }

    private fun showStatus(msg: String, showToast: Boolean = false) {

        status.append("$msg\n")

        if (showToast)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    }

    private fun requiredPermission(): Boolean {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED)
                return true
        }

        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == ACTIVITY_RECOGNITION) {
            if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                showStatus("Permission Granted")
            } else {
                showStatus("Permission Denied", true)
            }
        }
    }

}