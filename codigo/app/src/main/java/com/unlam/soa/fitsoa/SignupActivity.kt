package com.unlam.soa.fitsoa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.unlam.soa.api.ApiInterface
import com.unlam.soa.api.ResponseSignup
import com.unlam.soa.api.RetrofitInstance
import com.unlam.soa.models.UserBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {

    private var _nameText: EditText? = null
    private var _lastName: EditText? = null
    private var _emailText: EditText? = null
    private var _dniText: EditText? = null
    private var _passwordText: EditText? = null
    private var _reEnterPasswordText: EditText? = null
    private var _signupButton: Button? = null
    private var _loginLink: TextView? = null
    private var _progressBar: ProgressBar? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        setUpView()
    }

    private fun setUpView() {
        _nameText = findViewById<EditText>(R.id.input_name) as EditText
        _lastName = findViewById<EditText>(R.id.input_lastname) as EditText
        _emailText = findViewById<EditText>(R.id.input_email) as EditText
        _dniText = findViewById<EditText>(R.id.input_dni) as EditText
        _passwordText = findViewById<EditText>(R.id.input_password) as EditText
        _reEnterPasswordText = findViewById<EditText>(R.id.input_reEnterPassword) as EditText
        _progressBar = findViewById<ProgressBar>(R.id.progressbar) as ProgressBar

        _signupButton = findViewById<Button>(R.id.btn_signup) as Button
        _loginLink = findViewById<TextView>(R.id.link_login) as TextView

        setUpListeners()
    }

    private fun setUpListeners() {
        _signupButton!!.setOnClickListener {
            signup()
        }

        _loginLink!!.setOnClickListener {
            // Finish the registration screen and return to the Login activity
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }
    }

    fun signup() {
        if (!validate()) {
            onSignupFailed()
            return
        }

        _signupButton!!.isEnabled = false
        _progressBar!!.visibility = View.VISIBLE;
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        );

        val name = _nameText!!.text.toString()
        val lastname = _lastName!!.text.toString()
        val email = _emailText!!.text.toString()
        val dni = _dniText!!.text.toString().toInt()
        val password = _passwordText!!.text.toString()

        val retIn = RetrofitInstance.getRetrofitInstance().create(ApiInterface::class.java)
        val signUpInfo = UserBody(
            name,
            lastname,
            dni,
            email,
            password
        )

        retIn.registerUser(signUpInfo).enqueue(object : Callback<ResponseSignup> {
            override fun onFailure(call: Call<ResponseSignup>?, t: Throwable) {
                onSignupFailed()
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            override fun onResponse(
                call: Call<ResponseSignup>?,
                response: Response<ResponseSignup>?
            ) {
                if (response!!.body() == null || response.code() == 404) {
                    _progressBar!!.visibility = View.INVISIBLE;
                    onSignupFailed()
                    return
                }
                val responseBody = response!!.body() as ResponseSignup

                if (responseBody.state == "success") {
                    onSignupSuccess()
                } else {
                    Toast.makeText(this@SignupActivity, responseBody.msg, Toast.LENGTH_SHORT).show()
                    _signupButton!!.isEnabled = true
                }
                _progressBar!!.visibility = View.INVISIBLE;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        })

    }

    private fun onSignupSuccess() {
        _signupButton!!.isEnabled = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun onSignupFailed() {
        Toast.makeText(baseContext, "Register failed", Toast.LENGTH_LONG).show()
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        _signupButton!!.isEnabled = true
    }

    private fun validate(): Boolean {
        var valid = true

        val name = _nameText!!.text.toString()
        val lastname = _lastName!!.text.toString()
        val email = _emailText!!.text.toString()
        val dni = _dniText!!.text.toString()
        val password = _passwordText!!.text.toString()
        val reEnterPassword = _reEnterPasswordText!!.text.toString()

        if (name.isEmpty() || name.length < 3) {
            _nameText!!.error = "at least 3 characters"
            valid = false
        } else {
            _nameText!!.error = null
        }

        if (lastname.isEmpty() || lastname.length < 3) {
            _lastName!!.error = "at least 3 characters"
            valid = false
        } else {
            _lastName!!.error = null
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText!!.error = "enter a valid email address"
            valid = false
        } else {
            _emailText!!.error = null
        }

        if (dni.isEmpty() || dni.length < 7 || dni.length > 8) {
            _dniText!!.error = "Enter a valid DNI"
            valid = false
        } else {
            _dniText!!.error = null
        }

        if (password.isEmpty() || password.length < 8 || password.length > 20) {
            _passwordText!!.error = "between 8 and 20 alphanumeric characters"
            valid = false
        } else {
            _passwordText!!.error = null
        }

        if (reEnterPassword.isEmpty() || reEnterPassword != password) {
            _reEnterPasswordText!!.error = "Password Do not match"
            valid = false
        } else {
            _reEnterPasswordText!!.error = null
        }

        return valid
    }
}