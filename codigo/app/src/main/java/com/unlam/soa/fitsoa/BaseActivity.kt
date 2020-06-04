package com.unlam.soa.fitsoa

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.soywiz.klock.DateTime
import com.unlam.soa.api.ApiInterface
import com.unlam.soa.api.ResponseEvent
import com.unlam.soa.api.RetrofitInstance
import com.unlam.soa.models.EventBody
import com.unlam.soa.sharedPreferences.AppPreferences
import com.unlam.soa.utils.Utils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class BaseActivity : AppCompatActivity(), SensorEventListener {
    var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            Toast.makeText(this, "No Light Sensor !", Toast.LENGTH_SHORT).show()
        }
    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!Utils.isOnline(context)) {
                Toast.makeText(
                    this@BaseActivity,
                    "No tenes conexión a internet",
                    Toast.LENGTH_SHORT
                ).show()
                sendEvent("Internet", "ACTIVO", "Device not connected to Internet")

            } else sendEvent("Internet", "ACTIVO", "Device connected to Internet")
        }
    }

    fun getDateOfYear(): Int {
        return DateTime.now().dayOfYear
    }

    fun sendEvent(type: String, state: String, description: String) {
        if (AppPreferences.token == "") return

        val retIn = RetrofitInstance.getRetrofitInstance().create(ApiInterface::class.java)
        val eventInfo = EventBody(type, state, description)

        retIn.registerEvent(eventInfo).enqueue(object : Callback<ResponseEvent> {
            override fun onFailure(call: Call<ResponseEvent>?, t: Throwable) {
                Log.d("Event error", t.message!!)
            }

            override fun onResponse(
                call: Call<ResponseEvent>?,
                response: Response<ResponseEvent>?
            ) {
                if (response!!.body() == null || response.code() >= 400) {
                    val errorBody = JSONObject(response.errorBody()!!.string())
                    Log.d("Event error", errorBody.get("msg") as String)
                    return
                }

                val responseBody = response.body() as ResponseEvent

                if (responseBody.state == "success")
                    Log.d("Event success", responseBody.state)

            }
        })
    }

    override fun onStart() {
        super.onStart()

        val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(broadcastReceiver)
        sensorManager?.unregisterListener(this)
        sendEvent("Sensor", "INACTIVO", "Light sensor was unregistered")
    }

    override fun onResume() {
        super.onResume()

        sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    open fun onStepSensorChangedTriggered(event: SensorEvent) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_LIGHT) {
                val value: Float = event.values[0]
                val nightMode: Int = AppCompatDelegate.getDefaultNightMode()
                if (value < 3 && nightMode != AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    sendEvent("Sensor", "ACTIVO", "Night sensor changed - Theme updated")
                } else if (value > 10 && nightMode != AppCompatDelegate.MODE_NIGHT_NO) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    sendEvent("Sensor", "ACTIVO", "Light sensor changed - Theme updated")
                }
            } else {
                onStepSensorChangedTriggered(event)
            }
        }
    }
}