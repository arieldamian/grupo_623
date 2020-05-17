package com.unlam.soa.fitsoa
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.unlam.soa.sharedPreferences.AppPreferences

class MainActivity : AppCompatActivity() {

    private var _tokenText: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _tokenText = findViewById<TextView>(R.id.token) as TextView

        _tokenText!!.text = AppPreferences.token

    }
}
