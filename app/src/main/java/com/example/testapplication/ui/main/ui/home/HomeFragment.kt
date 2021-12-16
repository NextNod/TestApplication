package com.example.testapplication.ui.main.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.testapplication.*
import com.example.testapplication.databinding.FragmentHomeBinding
import com.example.testapplication.ui.main.RecyclerViewAdapter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()

    fun run() :String {
        val request = Request.Builder()
            .header("Access-Token", LocalData.token!!)
            .url("http://junior.balinasoft.com/api/image?page=0")
            .build()

        client.newCall(request).execute().use { response -> return response.body()!!.string() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        LocalData.list = mutableListOf()

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if(isOnline(root.context)) {
            lateinit var result: Response
            Thread {
                val tmp = run()
                result = Json.decodeFromString(tmp)
            }.apply {
                start()
                join()
            }

            val datas = Json.decodeFromJsonElement<List<Image>>(result.data!!)

            if (!datas.isNullOrEmpty()) {
                val db = Room
                    .databaseBuilder(root.context, AppDatabase::class.java, Constants.DBname)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build().dao()

                val dataBasePhotos = db!!.all()!!.toMutableList()
                datas.forEach {
                    lateinit var image: Bitmap
                    Thread {
                        val img = URL(it.url).openStream()
                        image = BitmapFactory.decodeStream(img)
                    }.apply {
                        start()
                        join()
                    }

                    val tmp = Photo(it.id, it.date, image, it.lng, it.lat)
                    val find = dataBasePhotos.find { it!!.id == tmp.id }
                    if(find != null)
                        dataBasePhotos.remove(find)
                    else
                        db.insert(tmp)

                    LocalData.list!!.add(tmp)
                }

                dataBasePhotos.forEach {
                    db.delete(it)
                }
            }
        } else {
            val db = Room
                .databaseBuilder(root.context, AppDatabase::class.java, Constants.DBname)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build().dao()

            db!!.all()?.forEach {
                LocalData.list!!.add(it!!)
            }
        }

        GridLayoutManager(
            binding.root.context,
            3,
            RecyclerView.VERTICAL,
            false
        ).apply {
            binding.photoList.layoutManager = this
        }

        LocalData.adapter = RecyclerViewAdapter(LocalData.list!!)
        binding.photoList.adapter = LocalData.adapter

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}