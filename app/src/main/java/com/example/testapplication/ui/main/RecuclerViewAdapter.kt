package com.example.testapplication.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import androidx.room.Room
import com.example.testapplication.*
import kotlinx.android.synthetic.main.custom_list_commets.view.*
import okhttp3.OkHttpClient
import okhttp3.Request

class RecyclerViewAdapter(private val photos: MutableList<Photo>)
    : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.recucler_view,parent,false)

        return ViewHolder(view)
    }

    private val client = OkHttpClient()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.setImageBitmap(photos[position].image)
        val date = Date()
        date.date = photos[position].data.toInt()
        val format = SimpleDateFormat(Constants.dateFormat)
        holder.data.text = format.format(date)

        holder.card.setOnClickListener {
            val intent = Intent(it.context, PhotoComment::class.java)
            intent.putExtra("idImage", photos[position].id)
            startActivity(it.context, intent, null)
        }

        holder.card.setOnLongClickListener {
            val alertDialog = AlertDialog.Builder(it.context)
                .setTitle("Delete image")
                .setMessage("Do you want to delete \"${format.format(date)}\"")
                .setNegativeButton("No") { dialog, _ ->
                    dialog.cancel()
                }
            val context = alertDialog.context
            alertDialog.setPositiveButton("Yes") { dialog, _ ->
                val db = Room.databaseBuilder(context, AppDatabase::class.java, "database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                    .dao()

                val id = LocalData.list!![position].id
                db!!.delete(LocalData.list!![position])
                LocalData.list!!.removeAt(position)
                LocalData.adapter!!.notifyDataSetChanged()

                Thread {
                    val request = Request.Builder()
                        .header("Access-Token", LocalData.token!!)
                        .delete()
                        .url("http://junior.balinasoft.com/api/image/${id}")
                        .build()

                    client.newCall(request).execute().use {
                        Log.i("Result", it.body()!!.string())
                    }
                }.apply {
                    start()
                    join()
                }

                dialog.cancel()
            }
            alertDialog.create().show()
            true
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val card: CardView = itemView.findViewById(R.id.cardView)
        val image: ImageView = itemView.findViewById(R.id.imageView)
        val data: TextView = itemView.findViewById(R.id.dataPhoto)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}

class CommetsAdapter(private val comments: MutableList<Comment>, private val imageId :Int)
    : RecyclerView.Adapter<CommetsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_list_commets,parent,false)

        return ViewHolder(view)
    }

    private val client = OkHttpClient()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            comment.text = comments[position].text
            card.setOnLongClickListener {
                AlertDialog.Builder(it.context)
                    .setTitle("Delete comment")
                    .setMessage("Do you want to delete that comment \"${comments[position].text}\"")
                    .setPositiveButton("Yes") { dialog, _ ->
                        Thread {
                            val request = Request.Builder()
                                .delete()
                                .url("http://junior.balinasoft.com/api/image/$imageId/comment/${comments[position].id}")
                                .header("Access-Token", LocalData.token!!)
                                .build()

                            lateinit var result :String
                            client.newCall(request).execute().use {
                                result = it.body()!!.string()
                            }
                        }.apply {
                            start()
                            join()
                        }
                        comments.removeAt(position)
                        notifyDataSetChanged()
                        dialog.cancel()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val card = itemView.commentCard
        val comment = itemView.comment
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}