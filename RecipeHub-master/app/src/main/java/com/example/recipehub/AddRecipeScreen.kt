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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.recipehub.AppInterface.ApiInterface
import com.example.recipehub.modle.Recipe
import com.example.recipehub.modle.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

class AddRecipeScreen : AppCompatActivity() {

    private lateinit var recipeImageView:ImageView
    private var imageUplodeFlag = false
    private lateinit var selectedImageUri: Uri
    private lateinit var titleField:TextInputEditText
    private lateinit var description:TextInputEditText
    private lateinit var ingredients:TextInputEditText
    private lateinit var progressBar: ProgressBar
    private lateinit var CreateBtnId:MaterialButton

    // Register the Activity Result Launcher
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                recipeImageView.setImageURI(it)
                imageUplodeFlag = true
            }
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_recipe_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
        //MARK:  App Bar

        val appbar = findViewById<MaterialToolbar>(R.id.AddRecipeBar)
        setSupportActionBar(appbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Feilds

        recipeImageView = findViewById(R.id.recipeImage)
        recipeImageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }


        titleField = findViewById<TextInputEditText>(R.id.titleField)
        description = findViewById<TextInputEditText>(R.id.description)
        ingredients = findViewById<TextInputEditText>(R.id.ingredients)

        progressBar = findViewById(R.id.progressBar)

       CreateBtnId = findViewById<MaterialButton>(R.id.CreateBtnId)
        CreateBtnId.setOnClickListener {
            showLoading(true) // Show progress bar and hide the button
            CreateBtnId.isEnabled = false // Disable the button
            val sharedPF = getSharedPreferences("RecipeHubSh", Context.MODE_PRIVATE)
            val token = sharedPF.getString("LoginToken", "")
            CreateRecipeClick(token,selectedImageUri,titleField.text.toString(),description.text.toString(),ingredients.text.toString())
        }

    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE // Show progress bar
            CreateBtnId.visibility = View.GONE // Hide the update button
        } else {
            progressBar.visibility = View.GONE // Hide progress bar
            CreateBtnId.visibility = View.VISIBLE // Show the update button
            CreateBtnId.isEnabled = true // Re-enable the button
        }
    }

    private fun CreateRecipeClick(token: String?, imageUri: Uri?, title: String, description: String,ingredients:String) {
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

        val recipetitle = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val recipeDes = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val resingredients = ingredients.toRequestBody("text/plain".toMediaTypeOrNull())

        val authorization = "Bearer $token"

        val call = apiService.createRecipe(authorization, file?.let { MultipartBody.Part.createFormData("RecipesImage", it.name, it.asRequestBody("image/*".toMediaTypeOrNull())) }, recipetitle,recipeDes,resingredients)
        call.enqueue(object : retrofit2.Callback<Recipe> {
            override fun onResponse(call: retrofit2.Call<Recipe>, response: retrofit2.Response<Recipe>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val responseData = response.body()
                    if (responseData != null) {
                        Toast.makeText(applicationContext, "Recipe Created Successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
                file?.delete() // Delete temporary file after response
            }

            override fun onFailure(call: retrofit2.Call<Recipe>, t: Throwable) {
                showLoading(false)
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                file?.delete() // Delete temporary file in case of failure
            }
        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home ->{
                finish()
                true
            }else ->{
                super.onOptionsItemSelected(item)
            }
        }
    }
}