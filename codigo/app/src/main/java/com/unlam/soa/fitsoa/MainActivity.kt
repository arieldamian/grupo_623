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
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.unlam.soa.sharedPreferences.AppPreferences
import org.eazegraph.lib.charts.BarChart
import org.eazegraph.lib.charts.PieChart
import org.eazegraph.lib.models.BarModel
import org.eazegraph.lib.models.PieModel
import java.lang.reflect.Type
import java.util.*
import kotlin.math.absoluteValue

val STEPS_KM = 0.00076

class MainActivity : BaseActivity(), SensorEventListener {

    private var _stepsChart: PieChart? = null
    private var _barChart: BarChart? = null

    private var running = false
    private var _averageText: TextView? = null
    private var _totalStepsText: TextView? = null
    private var sensorManager: SensorManager? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkLogin()
        setUpView()
        requestPermission()
        getLastSteps()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private fun setUpView() {
        _stepsChart = findViewById<PieChart>(R.id.steps)
        _barChart = findViewById<BarChart>(R.id.bargraph)
        _averageText = findViewById<TextView>(R.id.average)
        _totalStepsText = findViewById<TextView>(R.id.total)

        _stepsChart?.addPieSlice(
            PieModel(
                "Steps",
                AppPreferences.totalSteps,
                Color.parseColor("#6200EE")
            )
        )
        _stepsChart?.addPieSlice(PieModel("Today Steps", 0.0f, Color.parseColor("#000000")))
        _stepsChart?.startAnimation()
        _barChart?.startAnimation()
    }

    private fun requestPermission() {
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
    }

    private fun getLastSteps() {
        if (AppPreferences.stepsPerDay != "") {
            val stepsPerDay: TreeMap<Int, Float> =
                getStepsPerDay().toSortedMap(reverseOrder()) as TreeMap<Int, Float>

            if (!stepsPerDay.containsKey(getDateOfYear()))
                AppPreferences.lastDaySteps = stepsPerDay.values.elementAt(0).absoluteValue

            _averageText!!.text =
                "%.2f".format((AppPreferences.totalSteps / stepsPerDay.size) * STEPS_KM)
            _totalStepsText!!.text = "%.2f".format((AppPreferences.totalSteps) * STEPS_KM)
        } else {
            _averageText!!.text = "-"
            _totalStepsText!!.text = "-"
        }
    }

    private fun getStepsPerDay(): TreeMap<Int, Float> {
        if (AppPreferences.stepsPerDay == "") return TreeMap<Int, Float>()
        return Gson().fromJson(
            AppPreferences.stepsPerDay,
            object : TypeToken<TreeMap<Int?, Float?>?>() {}.type
        )
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
            val stepsPerDay: TreeMap<Int, Float> =
                getStepsPerDay().toSortedMap(reverseOrder()) as TreeMap<Int, Float>
            checkSteps(event.values[0])
            AppPreferences.totalSteps = event.values[0]

            generateBarChart()
            generatePieChart(stepsPerDay);

            _averageText!!.text =
                "%.2f".format((AppPreferences.totalSteps / stepsPerDay.size) * STEPS_KM)
            _totalStepsText!!.text = "%.2f".format((AppPreferences.totalSteps) * STEPS_KM)
        }
    }

    private fun checkSteps(steps: Float) {
        val dayOfYear: Int = getDateOfYear()
        val stepsPerDay: TreeMap<Int, Float> =
            getStepsPerDay().toSortedMap(reverseOrder()) as TreeMap<Int, Float>
        val realSteps: Float =
            if (steps < AppPreferences.totalSteps) AppPreferences.totalSteps else steps
        if (AppPreferences.stepsPerDay != "") {
            stepsPerDay[dayOfYear] = realSteps - AppPreferences.lastDaySteps
        } else {
            stepsPerDay[dayOfYear] = realSteps
        }
        storeStepsPerDay(stepsPerDay)
    }

    private fun storeStepsPerDay(stepsPerDay: TreeMap<Int, Float>) {
        // convert map to JSON String

        val builder = GsonBuilder()
        val gson = builder.enableComplexMapKeySerialization().setPrettyPrinting().create()
        val type: Type = object : TypeToken<TreeMap<Int?, Float?>?>() {}.type
        val stringMap: String = gson.toJson(stepsPerDay, type)
        AppPreferences.stepsPerDay = stringMap
    }

    private fun generateBarChart() {
        var days = 7
        var bm: BarModel
        if (_barChart!!.data.size > 0) _barChart!!.clearChart();
        if (AppPreferences.stepsPerDay != "") {
            val stepsPerDay: TreeMap<Int, Float> =
                getStepsPerDay().toSortedMap(reverseOrder()) as TreeMap<Int, Float>
            for ((k) in stepsPerDay) {
                bm = BarModel("Day $days", stepsPerDay[k]!!, Color.parseColor("#99CC00"))
                days--
                _barChart!!.addBar(bm);
            }
            for (i in days downTo 0 step 1) {
                bm = BarModel("Day $days", 0.0f, Color.parseColor("#99CC00"))
                days--
                _barChart!!.addBar(bm);
            }
        }
    }

    private fun generatePieChart(stepsPerDay: TreeMap<Int, Float>) {
        val dayOfYear: Int = getDateOfYear()

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
                stepsPerDay[dayOfYear] ?: 0.0f,
                Color.parseColor("#000000")
            )
        )
    }
}
