package com.example.recipehub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.recipehub.Adpter.DataBaseHelper
import com.example.recipehub.Adpter.LocalDataAdapter
import com.example.recipehub.modle.Recipe
import com.google.android.material.appbar.MaterialToolbar

class UserSavedStuff : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var swipe:SwipeRefreshLayout
    private lateinit var list:List<Recipe>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_saved_stuff)
        val profileBar = findViewById<MaterialToolbar>(R.id.userStuff)
        setSupportActionBar(profileBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val db = DataBaseHelper(this)
        recycler = findViewById(R.id.recyclerUD)

        swipe = findViewById(R.id.swipeRefreshUD)

        list = db.fetchAll()
        val adp = LocalDataAdapter(this,list){ recipe:Recipe ->
            val intent = Intent(applicationContext,DetailViewActivity::class.java)
            intent.putExtra("title",recipe.title)
            intent.putExtra("description",recipe.description)
            intent.putExtra("image",recipe.image)
            intent.putExtra("ingredients",recipe.ingredients)
            startActivity(intent)
        }

        recycler.adapter = adp
        recycler.layoutManager = LinearLayoutManager(this@UserSavedStuff)

        swipe.setOnRefreshListener {
            list = db.fetchAll()
            swipe.isRefreshing = false
            adp.notifyDataSetChanged()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}