package com.aceplus.steptracker

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.aceplus.steptracker.App.Companion.CHANNEL_ID


class StepCountingService: Service(), SensorEventListener {

    companion object {
        const val BROADCAST_ACTION = "com.aceplus.steptracker"
        const val COUNT_RESULT = "count_result"
        const val DETECT_RESULT = "detect_result"
    }

    private var totalStepCount = 0
    private var totalDetection = 0
    private var serviceRunning = false
    private var broadCastIntent: Intent? = null


    override fun onCreate() {
        super.onCreate()
        broadCastIntent = Intent(BROADCAST_ACTION)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        serviceRunning = true
        showNotification()

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detectSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, detectSensor, SensorManager.SENSOR_DELAY_NORMAL)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /*override fun onTaskRemoved(rootIntent: Intent?) {
        initAlarm()
        super.onTaskRemoved(rootIntent)
    }*/

    override fun onDestroy() {
        super.onDestroy()
        serviceRunning = false
    }

    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            totalStepCount = event.values[0].toInt()
        }
        else if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            totalDetection++
        }

        broadcastSensorValues()

    }

    private fun broadcastSensorValues() {

        if (!serviceRunning) return

        broadCastIntent?.putExtra("count_result", totalStepCount)
        broadCastIntent?.putExtra("detect_result", totalDetection)

        sendBroadcast(broadCastIntent)
    }

    private fun showNotification() {

        val homeIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, homeIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Counter")
            .setContentText("step counter is running in background")
            //.setPriority(Notification.PRIORITY_MIN)
            .setSmallIcon(R.drawable.ic_noti_step_counter)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(3, notification)

    }

    private fun initAlarm() {
        val alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, StepCountingService::class.java)
        val alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        alarmMgr[AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 2000] = alarmIntent
    }

}