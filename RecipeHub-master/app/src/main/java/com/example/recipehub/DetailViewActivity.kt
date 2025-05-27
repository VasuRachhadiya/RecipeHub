package com.example.recipehub

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.squareup.picasso.Picasso

class DetailViewActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_view)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
        val toolBar = findViewById<MaterialToolbar>(R.id.detID)
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        val imageview= findViewById<ImageView>(R.id.imageView4)
        val url = intent.getStringExtra("image")
        Picasso.get().load(url).into(imageview)

        val title = findViewById<TextView>(R.id.textView)
        title.text = intent.getStringExtra("title")

        val des = findViewById<TextView>(R.id.des)
        des.text = intent.getStringExtra("description")

        val ing = findViewById<TextView>(R.id.ing)
        ing.text = intent.getStringExtra("ingredients")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                finish()
                return  true
            }else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}