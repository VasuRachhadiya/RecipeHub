package com.example.recipehub

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.recipehub.AppInterface.ApiInterface
import com.example.recipehub.modle.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

class EditProfileScreen : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar // Reference for ProgressBar
    private lateinit var updateButton: MaterialButton // Reference for the update button
    private var imageUplodeFlag = false
    private lateinit var selectedImageUri: Uri

    // Register the Activity Result Launcher
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                imageView.setImageURI(it)
                imageUplodeFlag = true
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_screen)
        val editprofilebar = findViewById<MaterialToolbar>(R.id.editprofilebar)
        setSupportActionBar(editprofilebar)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        imageView = findViewById(R.id.uploadImage)
        progressBar = findViewById(R.id.progressBar)
        updateButton = findViewById(R.id.uplodebtn)

        val email = findViewById<TextInputEditText>(R.id.email)
        val username = findViewById<TextInputEditText>(R.id.username)

        val sharedPF = getSharedPreferences("RecipeHubSh", Context.MODE_PRIVATE)

// Retrieve and set the email
        val savedEmail = sharedPF.getString("email", null)
        email.setText(savedEmail ?: "") // Avoid using 'also' here; explicitly set the text

// Retrieve and set the profile picture (optional, depending on usage)
        val profilePicture = sharedPF.getString(
            "profilePicture",
            "https://miro.medium.com/v2/resize:fit:1400/1*MXyMqcEJ6Se0SCWcYCKZTQ.jpeg"
        )

        Picasso.get().load(profilePicture).into(imageView)

// Retrieve and set the username
        val savedUsername = sharedPF.getString("username", "OOPS Server Error!")
        username.setText(savedUsername)
        // ImageView click listener for picking an image
        imageView.setOnClickListener {
            //MARK: select image
            pickImageLauncher.launch("image/*")
        }

        // Update button click listener
        updateButton.setOnClickListener {
            showLoading(true) // Show progress bar and hide the button
            updateButton.isEnabled = false // Disable the button
            val token = sharedPF.getString("LoginToken", "") // Get token from SharedPreferences

            if (imageUplodeFlag) {
                updateProfile(token = token ?: "", imageUri = selectedImageUri, username = username.text.toString(), email = email.text.toString())
            } else {
                updateProfile(token = token ?: "", imageUri = null, username = username.text.toString(), email = email.text.toString())
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE // Show progress bar
            updateButton.visibility = View.GONE // Hide the update button
        } else {
            progressBar.visibility = View.GONE // Hide progress bar
            updateButton.visibility = View.VISIBLE // Show the update button
            updateButton.isEnabled = true // Re-enable the button
        }
    }

    private fun updateProfile(token: String?, imageUri: Uri?, username: String, email: String) {
        val file: File? = if (imageUri != null) {
            val fileDir = applicationContext.cacheDir
            val tempFile = File(fileDir, "image.jpeg")
            val inputStream = contentResolver.openInputStream(imageUri)
            val outputStream = FileOutputStream(tempFile)
            inputStream!!.copyTo(outputStream)

            val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            tempFile // Store file reference for deletion later
        } else {
            null
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://sharerecipy-backend.onrender.com/") // Use your actual backend URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiInterface::class.java)

        val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())

        val authorization = "Bearer $token"

        val call = apiService.updateProfile(authorization, file?.let { MultipartBody.Part.createFormData("RecipyUserProfile", it.name, it.asRequestBody("image/*".toMediaTypeOrNull())) }, usernamePart, emailPart)
        call.enqueue(object : retrofit2.Callback<User> {
            override fun onResponse(call: retrofit2.Call<User>, response: retrofit2.Response<User>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val responseData = response.body()
                    if (responseData != null) {
                        val sharedPreferences = getSharedPreferences("RecipeHubSh", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("username", responseData.username)
                        editor.putString("email", responseData.email)
                        editor.putString("profilePicture", responseData.profilePicture)
                        editor.apply()

                        Toast.makeText(applicationContext, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
                file?.delete() // Delete temporary file after response
            }

            override fun onFailure(call: retrofit2.Call<User>, t: Throwable) {
                showLoading(false)
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                file?.delete() // Delete temporary file in case of failure
            }
        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         return when(item.itemId){
             android.R.id.home -> {
                 finish()
                 true
             }else -> {
                 super.onOptionsItemSelected(item)
             }
         }
    }
}




/*


    Mulitipart Form Request Notes


    we have to create fiel Dir in app dir
    val fileDir = applicationContext.filesDir
    val file = File(fileDir,"image.png")

    user select image data copy in that file
    val inputStream = contentResolver.openInputStream(imageUri) // imageUri is selected user imaeg that copy in new File iamge
    val outputStrem = FileOutputStream(file)
    inputStream!!.copyTo(outputStrem)


 */
