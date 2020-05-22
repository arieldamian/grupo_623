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
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.unlam.soa.sharedPreferences.AppPreferences
import org.eazegraph.lib.charts.PieChart
import org.eazegraph.lib.models.PieModel
import java.lang.reflect.Type
import java.util.*
import kotlin.math.absoluteValue


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
        _stepsChart?.addPieSlice(PieModel("Steps",AppPreferences.totalSteps, Color.parseColor("#6200EE")))
        _stepsChart?.addPieSlice(PieModel("Today Steps", 0.0f, Color.parseColor("#000000")))
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

        getLastSteps()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

    }

    private fun getLastSteps(){
        if(AppPreferences.stepsPerDay != ""){
            val stepsPerDay:  TreeMap<Int, Float> = getStepsPerDay().toSortedMap(reverseOrder()) as TreeMap<Int, Float>
            val dayOfYear: Int = getDateOfYear()

            if(!stepsPerDay.containsKey(dayOfYear))
                AppPreferences.lastDaySteps = stepsPerDay.values.elementAt(0).absoluteValue
        }
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
        getLastSteps()
    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorManager?.unregisterListener(this)
    }

    private fun getStepsPerDay(): TreeMap<Int, Float> {
        if(AppPreferences.stepsPerDay == "") return TreeMap<Int,Float>()
        return Gson().fromJson(
            AppPreferences.stepsPerDay,
            object : TypeToken<TreeMap<Int?, Float?>?>() {}.type
        )
    }

    private fun checkSteps(steps : Float){
        val dayOfYear: Int = getDateOfYear()
        val stepsPerDay:  TreeMap<Int, Float> = getStepsPerDay().toSortedMap(reverseOrder()) as TreeMap<Int, Float>
        val realSteps: Float = if(steps < AppPreferences.totalSteps) AppPreferences.totalSteps else steps
        if(AppPreferences.stepsPerDay != "") {
            stepsPerDay[dayOfYear] = realSteps - AppPreferences.lastDaySteps
        }else {
            stepsPerDay[dayOfYear] = realSteps
        }
        updateStepsPerDay(stepsPerDay)
    }

    private fun updateStepsPerDay(stepsPerDay : TreeMap<Int, Float>) {
        // convert map to JSON String

        val builder = GsonBuilder()
        val gson = builder.enableComplexMapKeySerialization().setPrettyPrinting().create()
        val type: Type = object : TypeToken<TreeMap<Int?, Float?>?>() {}.type
        val stringMap: String = gson.toJson(stepsPerDay, type)
        AppPreferences.stepsPerDay = stringMap
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
            Log.d("Steps", event.values[0].toString())
            val stepsPerDay:  TreeMap<Int, Float> = getStepsPerDay().toSortedMap(reverseOrder()) as TreeMap<Int, Float>
            val dayOfYear: Int = getDateOfYear()
            checkSteps(event.values[0])
            AppPreferences.totalSteps = event.values[0]
            _stepsChart?.clearChart();
            _stepsChart?.addPieSlice(
                PieModel(
                    "Total Steps",
                    AppPreferences.totalSteps,
                    Color.parseColor("#6200EE")
                )
            )
            _stepsChart?.addPieSlice(
                PieModel(
                    "Today Steps",
                    stepsPerDay[dayOfYear]?: 0.0f,
                    Color.parseColor("#000000")
                )
            )
        }
    }
}
