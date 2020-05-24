package com.unlam.soa.fitsoa

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.soywiz.klock.DateTime
import com.unlam.soa.api.ApiInterface
import com.unlam.soa.api.ResponseEvent
import com.unlam.soa.api.ResponseLogin
import com.unlam.soa.api.RetrofitInstance
import com.unlam.soa.models.EventBody
import com.unlam.soa.models.SignInBody
import com.unlam.soa.utils.Utils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class BaseActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
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

            }else sendEvent("Internet", "ACTIVO", "Device connected to Internet")
        }
    }

    fun getDateOfYear(): Int {
        val time = DateTime.now()
        return time.dayOfYear
    }

    fun sendEvent(type: String, state:String,description: String) {
        val retIn = RetrofitInstance.getRetrofitInstance().create(ApiInterface::class.java)
        val eventInfo = EventBody(type, state,description)

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
                    Log.d("Event error", errorBody.get("msg") as String )
                    return
                }

                val responseBody = response.body() as ResponseEvent

                if (responseBody.state == "success")
                    Log.d("Event success",responseBody.state)

            }
        })
    }

    override fun onStart() {
        super.onStart()

        val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()

        unregisterReceiver(broadcastReceiver)
    }
}