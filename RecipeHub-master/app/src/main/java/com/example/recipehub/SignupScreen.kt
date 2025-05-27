package com.example.recipehub

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import com.example.recipehub.AppInterface.ApiInterface
import com.example.recipehub.modle.AuthModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignupScreen : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
        val toolBar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val email = findViewById<TextInputEditText>(R.id.emailField)
        val username = findViewById<TextInputEditText>(R.id.usernameFeild)
        val password = findViewById<TextInputEditText>(R.id.passwordFeild)

        val signupButton = findViewById<MaterialButton>(R.id.signup)
        signupButton.setOnClickListener {


            if (email.text!!.isNotEmpty() && username.text!!.isNotEmpty() && password.text!!.isNotEmpty()) {
                val loginParam = mapOf(
                    "email" to email.text.toString(),
                    "password" to password.text.toString(),
                    "username" to username.text.toString()
                )
                SignupClick(loginParam)
            } else {
                // Handle the case when either field is empty
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
                val errorView = findViewById<TextView>(R.id.textView3)
                errorView.text = "Fields are Empety"
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun SignupClick(param:Map<String,String>){
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://sharerecipy-backend.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)

        val  retrodata = retrofitBuilder.Signup(param)
        retrodata.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val statusCode = response.code()
                if(statusCode == 201){
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val jsonString = responseBody.string()
                        val actualData = Gson().fromJson(jsonString, AuthModel::class.java)
                        val sharedPF = getSharedPreferences("RecipeHubSh", Context.MODE_PRIVATE)
                        var editorsh = sharedPF.edit()
                        editorsh.putString("LoginToken",actualData.Logintoken)
                        editorsh.putString("username",actualData.user.username)
                        editorsh.putString("email",actualData.user.email)
                        editorsh.putString("profilePicture",actualData.user.profilePicture)
                        editorsh.apply()
                        val intent = Intent(applicationContext,HomeScreen::class.java)
                        startActivity(intent)
                        finish()
                        // Add Data using Editorsh
//                        Toast.makeText(applicationContext, actualData.Logintoken, Toast.LENGTH_LONG).show()
                    } else {
                        // Handle the case where the response body is null
                        Toast.makeText(applicationContext, "Sorry, something went wrong. Please try again later.", Toast.LENGTH_LONG).show()
                        val errorView = findViewById<TextView>(R.id.textView3)
                        errorView.text = "Sorry, something went wrong. Please try again later."
                    }
                }else{
                    // Handle Server other Status Code MYError
                    Toast.makeText(applicationContext,statusCode.toString(),Toast.LENGTH_LONG).show()
                    val errorView = findViewById<TextView>(R.id.textView3)
                    errorView.text = "Server Not Responding."
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(applicationContext, "MYError: ${t.message}", Toast.LENGTH_LONG).show()
                val errorView = findViewById<TextView>(R.id.textView3)
                errorView.text = "Sorry, something went wrong. Please try again later."
            }
        })
    }
}