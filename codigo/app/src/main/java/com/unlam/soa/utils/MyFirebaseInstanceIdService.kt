package com.unlam.soa.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.unlam.soa.api.ApiInterface
import com.unlam.soa.api.MessageResponse
import com.unlam.soa.api.ResponseNotification
import com.unlam.soa.fitsoa.R
import com.unlam.soa.models.NotificationBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyFirebaseInstanceIdService : FirebaseMessagingService() {
    val TAG = "PushNotifService"
    lateinit var name: String

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.e("NEW_TOKEN", s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "FitSOA: ${remoteMessage.from}")
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "FitSOA_Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Your Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.description = "Description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // to diaplay notification in DND Mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            channel.canBypassDnd()
        }

        val gson = Gson()

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val message =
            gson.fromJson(remoteMessage.data.values.elementAt(0), MessageResponse::class.java)
        notificationBuilder.setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.design_default_color_primary))
            .setContentTitle("FitSOA- " + message.title)
            .setContentText(message.message)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.fitsoa)
            .setAutoCancel(true)


        notificationManager.notify(1000, notificationBuilder.build())

    }

    companion object {
        fun sendNotification(title: String, message: String) = run {

            FirebaseInstanceId.getInstance().instanceId
                .addOnSuccessListener { instanceIdResult ->
                    val retrofitInstance = Retrofit.Builder()
                        .baseUrl("https://fcm.googleapis.com/fcm/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val token = instanceIdResult.token //Token
                    val dataObject = JSONObject()

                    dataObject.put("title", title)
                    dataObject.put("message", message)
                    val notificationInfo = NotificationBody(token, dataObject)
                    val retIn = retrofitInstance.create(ApiInterface::class.java)
                    retIn.sendNotification(notificationInfo)
                        .enqueue(object : Callback<ResponseNotification> {
                            override fun onFailure(
                                call: Call<ResponseNotification>?,
                                t: Throwable
                            ) {
                                Log.d("Notification Error", t.message!!)
                            }

                            override fun onResponse(
                                call: Call<ResponseNotification>,
                                response: Response<ResponseNotification>
                            ) {
                                Log.d("Notification Success", "Success")
                            }
                        })
                }
        }
    }

}