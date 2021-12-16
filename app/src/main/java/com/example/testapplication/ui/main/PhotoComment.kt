package com.example.testapplication.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testapplication.Comment
import com.example.testapplication.Constants
import com.example.testapplication.LocalData
import com.example.testapplication.Response
import com.example.testapplication.databinding.ActivityPhotoCommentBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*

class PhotoComment : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoCommentBinding
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idImage = intent.extras!!.getInt("idImage")

        val photo = LocalData.list!!.find { it.id == idImage }
        binding.commentedImage.setImageBitmap(photo!!.image)
        val date = Date()
        date.date = photo!!.data.toInt()
        val format = SimpleDateFormat("dd.MM.yyyy")
        binding.nameComentedImage.text = format.format(date)

        val request = Request.Builder()
            .header("Access-Token", LocalData.token!!)
            .get()
            .url("http://junior.balinasoft.com/api/image/$idImage/comment?page=0")
            .build()

        lateinit var result: Response
        Thread {
            client.newCall(request).execute().use { request ->
                result = Json.decodeFromString(request.body()!!.string())
            }
        }.apply {
            start()
            join()
        }

        var comments: MutableList<Comment>? = null
        var adapter :CommetsAdapter? = null

        if(result.status == 200) {
            comments = Json.decodeFromJsonElement<List<Comment>>(result.data!!).toMutableList()
            adapter = CommetsAdapter(comments, idImage)
            binding.listOfComments.apply {
                layoutManager = LinearLayoutManager(binding.root.context)
                this.adapter = adapter
            }
        }

        binding.sendButton.setOnClickListener {
            val message = binding.commentText.text.toString()
            val request = Request.Builder()
                .post(RequestBody.create(Constants.JSON, "{\n\"text\":\"$message\"\n}"))
                .header("Access-Token", LocalData.token!!)
                .url("http://junior.balinasoft.com/api/image/$idImage/comment")
                .build()

            Thread {
                client.newCall(request).execute().use {
                    result = Json.decodeFromString(it.body()!!.string())
                }
            }.apply {
                start()
                join()
            }

            if(result.status == 200 && adapter != null) {
                val tmp = Json.decodeFromJsonElement<Comment>(result.data!!)
                binding.commentText.text.clear()
                comments!!.add(tmp)
                adapter.notifyDataSetChanged()
            }
        }
    }
}