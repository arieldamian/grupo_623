package com.unlam.soa.fitsoa

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.unlam.soa.sharedPreferences.AppPreferences
import org.eazegraph.lib.charts.PieChart
import org.eazegraph.lib.models.PieModel

class MainActivity : BaseActivity(), SensorEventListener {

    private var _stepsChart: PieChart? = null
    private var running = false
    private var sensorManager: SensorManager? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLogin()

        setContentView(R.layout.activity_main)
        _stepsChart = findViewById<PieChart>(R.id.steps)
        _stepsChart?.addPieSlice(PieModel("Steps", 0F, Color.parseColor("#6200EE")))
        _stepsChart?.startAnimation();

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                Array<String>(1) { Manifest.permission.ACTIVITY_RECOGNITION },
                123
            );
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

    }

    override fun onResume() {
        super.onResume()
        checkLogin()
        running = true
        val stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepsSensor == null) {
            Toast.makeText(this, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorManager?.unregisterListener(this)
    }

    private fun checkLogin() {
        if (!AppPreferences.isLogged) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (running) {
            Log.d("Steps", event.values[0].toString());
            _stepsChart?.clearChart();
            _stepsChart?.addPieSlice(
                PieModel(
                    "Steps",
                    event.values[0],
                    Color.parseColor("#6200EE")
                )
            )
        }
    }
}
