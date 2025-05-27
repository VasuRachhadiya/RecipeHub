package com.example.recipehub.Adpter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipehub.R
import com.example.recipehub.modle.Recipe
import com.squareup.picasso.Picasso

class LocalDataAdapter(val Conent: Activity, val recipes: List<Recipe>, val getRecipe:(item: Recipe)->Unit) : RecyclerView.Adapter<LocalDataAdapter.LocalDataRecipeViewHolder>() {

    class LocalDataRecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var item:CardView = view.findViewById(R.id.item)
        val imageView: ImageView = view.findViewById(R.id.savedImage)
        val description: TextView = view.findViewById(R.id.savedTitle)
        val share:ImageButton = view.findViewById(R.id.shareData)
        val delete = view.findViewById<ImageButton>(R.id.delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalDataRecipeViewHolder {
        val layout = LayoutInflater.from(Conent).inflate(R.layout.userdataitem, parent, false)
        return LocalDataRecipeViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    override fun onBindViewHolder(holder: LocalDataRecipeViewHolder, position: Int) {
        val recipe = recipes[position]


        holder.description.text = recipe.title ?: "No title available"

        val imageUrl = recipe.image
        if (imageUrl != null) {
            Picasso.get().load(imageUrl).into(holder.imageView)
        } else {
            // Fallback image
            Picasso.get().load("https://miro.medium.com/v2/resize:fit:1400/1*MXyMqcEJ6Se0SCWcYCKZTQ.jpeg").into(holder.imageView)
        }

        holder.item.setOnClickListener {
            getRecipe(recipe)
        }


        holder.share.setOnClickListener {
            val message = """
        🍽️ *Recipe Name:* ${recipe.title}
        📝 *Description:* ${recipe.description}
        🥕 *Ingredients:* ${recipe.ingredients}
        📷 *Image:* ${recipe.image}
    """
            // Create share intent
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain" // Set type as image
                putExtra(Intent.EXTRA_TEXT, message) // Attach text
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant permission to read image
            }

            // Show app chooser
            Conent.startActivity(Intent.createChooser(intent, "Share Recipe"))
        }

        holder.delete.setOnClickListener {
            val dbHelper = DataBaseHelper(Conent)
            val rowsDeleted = recipe._id?.let { it1 -> dbHelper.deleteByRid(it1) }

            if (rowsDeleted != null) {
                if (rowsDeleted > 0) {
                    Toast.makeText(Conent,"Delete successfully,Refresh to see Changes",Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(Conent,"Fail to delete",Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}