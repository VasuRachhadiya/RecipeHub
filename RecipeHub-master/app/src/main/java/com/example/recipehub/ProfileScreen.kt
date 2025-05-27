package com.example.recipehub

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipehub.Adpter.GridAdpater
import com.example.recipehub.AppInterface.ApiInterface
import com.example.recipehub.modle.AuthModel
import com.example.recipehub.modle.MYError
import com.example.recipehub.modle.Message
import com.example.recipehub.modle.Recipe
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class ProfileScreen : AppCompatActivity() {

    private var currentPage = 1
    private var isLoading = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GridAdpater
    private val recipeList = mutableListOf<Recipe>()
    private lateinit var progressBar: ProgressBar
    private lateinit var shimmer:ShimmerFrameLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT

        val sharedPF = getSharedPreferences("RecipeHubSh", MODE_PRIVATE)
        val email = sharedPF.getString("email","Error")
        val profilePicture = sharedPF.getString("profilePicture","https://miro.medium.com/v2/resize:fit:1400/1*MXyMqcEJ6Se0SCWcYCKZTQ.jpeg")
        val username = sharedPF.getString("username","OOPS Server Error!")

        shimmer = findViewById(R.id.gridShimmer)

        val profileBar = findViewById<MaterialToolbar>(R.id.profilebar)
        setSupportActionBar(profileBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        profileBar.title = username.toString()

        val editButton = findViewById<MaterialButton>(R.id.editProfile)
        editButton.setOnClickListener{
            val intent = Intent(applicationContext,EditProfileScreen::class.java)
            startActivity(intent)
        }

        val addRecipy = findViewById<MaterialButton>(R.id.addRecipe)
        addRecipy.setOnClickListener{
            val intent = Intent(applicationContext,AddRecipeScreen::class.java)
            startActivity(intent)
        }

        val imageView = findViewById<ImageView>(R.id.imageView3)
        Picasso.get().load(profilePicture).into(imageView)


        val emailHolder = findViewById<TextView>(R.id.textView2)
        emailHolder.text = email

        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.asGridUser)
        adapter = GridAdpater(this@ProfileScreen, recipeList){ id ->
            deleteRecipe(id)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(20))

        loadRecipes(currentPage)

        // Scroll Listener for Pagination
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    currentPage++
                    loadRecipes(currentPage)
                }
            }
        })
    }

    private fun loadRecipes(page: Int) {
        isLoading = true
        if(page == 1){
            shimmer.startShimmer()
        }else{
            progressBar.visibility = View.VISIBLE
        }
        NetworkCallListRecipe(page) { list, error ->
            isLoading = false
            progressBar.visibility = View.GONE
            shimmer.stopShimmer()
            shimmer.visibility = View.GONE
            if (error == null && list != null) {
                recipeList.addAll(list)
                findViewById<RecyclerView>(R.id.asGridUser).visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
            } else {
                Log.e("API_ERROR", "Error: $error")
                val errorLable = findViewById<TextView>(R.id.errorLable)
                errorLable.text = error.toString()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profilemenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.logout ->{
                val shared = getSharedPreferences("RecipeHubSh", MODE_PRIVATE)
                val edit = shared.edit()
                edit.remove("LoginToken")
                edit.apply()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish();
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun NetworkCallListRecipe(page: Int, callback: (List<Recipe>?, String?) -> Unit) {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://sharerecipy-backend.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)

        val sharedPF = getSharedPreferences("RecipeHubSh", MODE_PRIVATE)
        val token = sharedPF.getString("LoginToken", null)

        if (token != null) {
            val retroData = retrofitBuilder.ListUserRecipe(page, token)
            retroData.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val statusCode = response.code()
                    if (statusCode == 200) {
                        val recipeList = Gson().fromJson(response.body()?.string(), Array<Recipe>::class.java).toList()
                        callback(recipeList, null)
                    } else {
                        callback(null, parseErrorResponse(response.body()))
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    if (t is IOException) {
                        callback(null, "Network error, please check your connection.")
                    } else {
                        callback(null, "Unexpected error: Fail to fetch")
                    }
                }
            })
        } else {
            callback(null, "Token is missing")
        }
    }

    private fun parseErrorResponse(responseBody: ResponseBody?): String {
        return try {
            val jsonString = responseBody?.string()
            val myError = Gson().fromJson(jsonString, MYError::class.java)
            myError.technicalDetails
        } catch (e: Exception) {
            Log.e("API_ERROR", "Response: $responseBody")
            "Failed Netwrok Problem"
        }
    }

    private fun deleteRecipe(id:String){
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://sharerecipy-backend.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)

        val sharedPF = getSharedPreferences("RecipeHubSh", MODE_PRIVATE)
        val token = sharedPF.getString("LoginToken", null)
        val mapdata = mapOf(
            "_id" to id
        )

        if(token != null){
            val  retrodata = retrofitBuilder.DeleteUserRecipe(token,mapdata)
            retrodata.enqueue(object : Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    val statusCode = response.code()
                    if(statusCode == 201) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            val jsonString = responseBody.string()
                            val actualData = Gson().fromJson(jsonString, Message::class.java)
                            Toast.makeText(this@ProfileScreen,actualData.message ?: "Delete Record",Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(this@ProfileScreen,"Server error ${statusCode} ,Retry 😥",Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@ProfileScreen,"Netwrok Error,Retry 😥",Toast.LENGTH_LONG).show()
                }
            })
        }else{
            Toast.makeText(this@ProfileScreen,"Netwrok Error,Retry \uD83D\uDE25",Toast.LENGTH_LONG).show()
        }
    }


}


private class GridSpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.left = spacing
        outRect.right = spacing
        outRect.bottom = spacing
        // Add top margin only for the first row
        if (parent.getChildAdapterPosition(view) < 3) { // Adjust based on span count
            outRect.top = spacing
        }
    }
}
