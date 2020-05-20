package com.unlam.soa.fitsoa
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.unlam.soa.sharedPreferences.AppPreferences

class MainActivity : BaseActivity() {

    private var _tokenText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkLogin()

        setContentView(R.layout.activity_main)
        _tokenText = findViewById<TextView>(R.id.token) as TextView
        _tokenText!!.text = AppPreferences.token
    }

    override fun onResume() {
        super.onResume()

        checkLogin()
    }

    private fun checkLogin() {
        if (!AppPreferences.isLogged) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
