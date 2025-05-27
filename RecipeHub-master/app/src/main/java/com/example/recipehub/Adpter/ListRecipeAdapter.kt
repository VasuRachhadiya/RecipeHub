package com.example.recipehub.Adpter

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipehub.R
import com.example.recipehub.modle.Recipe
import com.squareup.picasso.Picasso
import android.content.Intent
import android.widget.Toast


class ListRecipeAdapter(val Conent:Activity,val recipes: List<Recipe>,val getRecipe:(item:Recipe)->Unit) : RecyclerView.Adapter<ListRecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card:CardView  = view.findViewById(R.id.card)
        val imageView: ImageView = view.findViewById(R.id.RecipeImage)
        val description: TextView = view.findViewById(R.id.description)
        val share:ImageButton = view.findViewById(R.id.share)
        var add:ImageButton = view.findViewById(R.id.add)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val layout = LayoutInflater.from(Conent).inflate(R.layout.eachrecipe, parent, false)
        return RecipeViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]

        val db = DataBaseHelper(Conent)

        val isit = recipe._id?.let { db.findImposter(it) }
        if(isit == true){
            holder.add.isEnabled = false
            holder.add.setImageResource(R.drawable.baseline_bookmark_24)
        }


        holder.description.text = recipe.description ?: "No description available"

        val imageUrl = recipe.image
        if (imageUrl != null) {
            Picasso.get().load(imageUrl).into(holder.imageView)
        } else {
           // Fallback image
            Picasso.get().load("https://miro.medium.com/v2/resize:fit:1400/1*MXyMqcEJ6Se0SCWcYCKZTQ.jpeg").into(holder.imageView)
        }

        holder.card.setOnClickListener {
            getRecipe(recipe)
        }
//
//        holder.imageView.setOnClickListener{
//            getRecipe(recipe)
//        }



        holder.add.setOnClickListener {
            val a = db.insertData(recipe)
            if(a == -1L){
                Toast.makeText(Conent,"Fail to save try! again 😥",Toast.LENGTH_LONG).show()
            }else{
                holder.add.isEnabled = false
                holder.add.setImageResource(R.drawable.baseline_bookmark_24)
            }
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
    }
}
