package com.unlam.soa.fitsoa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.unlam.soa.api.ApiInterface
import com.unlam.soa.api.ResponseLogin
import com.unlam.soa.api.RetrofitInstance
import com.unlam.soa.models.SignInBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private var _emailText: EditText? = null
    private var _passwordText: EditText? = null
    private var _loginButton: Button? = null
    private var _signupLink: TextView? = null
    private var _progressBar: ProgressBar? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setUpView()
    }

    private fun setUpView() {
        _loginButton = findViewById<Button>(R.id.btn_login) as Button
        _signupLink = findViewById<TextView>(R.id.link_signup) as TextView
        _passwordText = findViewById<EditText>(R.id.input_password) as EditText
        _emailText = findViewById<EditText>(R.id.input_email) as EditText
        _progressBar = findViewById<ProgressBar>(R.id.progressbar) as ProgressBar

        setUpListeners()
    }

    private fun setUpListeners() {
        _loginButton!!.setOnClickListener {
            login()
        }

        _signupLink!!.setOnClickListener {
            // Start the Signup activity
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun login() {
        if (!validate()) {
            onLoginFailed()
            return
        }

        _loginButton!!.isEnabled = false
        _progressBar!!.visibility = View.VISIBLE;
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        );

        val email = _emailText!!.text.toString()
        val password = _passwordText!!.text.toString()

        val retIn = RetrofitInstance.getRetrofitInstance().create(ApiInterface::class.java)
        val signInInfo = SignInBody(email, password)

        retIn.signin(signInInfo).enqueue(object : Callback<ResponseLogin> {
            override fun onFailure(call: Call<ResponseLogin>?, t: Throwable) {
                onLoginFailed()
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                _progressBar!!.visibility = View.INVISIBLE;
            }

            override fun onResponse(
                call: Call<ResponseLogin>?,
                response: Response<ResponseLogin>?
            ) {
                val responseBody = response!!.body() as ResponseLogin

                if (responseBody.state == "success") {
                    onLoginSuccess()
                } else {
                    Toast.makeText(this@LoginActivity, responseBody.msg, Toast.LENGTH_SHORT).show()
                    _loginButton!!.isEnabled = true
                }
                _progressBar!!.visibility = View.INVISIBLE;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        })
    }

    override fun onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true)
    }

    private fun onLoginSuccess() {
        _loginButton!!.isEnabled = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun onLoginFailed() {
        Toast.makeText(baseContext, "Login failed", Toast.LENGTH_LONG).show()

        _loginButton!!.isEnabled = true
    }

    private fun validate(): Boolean {
        var valid = true

        val email = _emailText!!.text.toString()
        val password = _passwordText!!.text.toString()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText!!.error = "Enter a valid email address"
            valid = false
        } else {
            _emailText!!.error = null
        }

        if (password.isEmpty() || password.length < 8 || password.length > 20) {
            _passwordText!!.error = "between 8 and 20 alphanumeric characters"
            valid = false
        } else {
            _passwordText!!.error = null
        }

        return valid
    }
}