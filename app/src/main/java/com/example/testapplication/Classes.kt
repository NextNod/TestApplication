package com.example.testapplication

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testapplication.ui.main.RecyclerViewAdapter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Login {
    @POST("api/account/signin")
    fun getSession(@Body account :RequestAccount) : Call<ResponseRetrofit>
}

@Serializable
data class Error(val field :String, val message :String)

@Serializable
data class Response(val status :Int, val error : String? = null, val valid :List<Error>? = null, val data :JsonElement? = null)

data class ResponseRetrofit(val status :Int, val data :Session, val error : String? = null, val valid :List<Error>? = null,)

@Serializable
data class Session(val userId :Int, val login :String, val token :String)

@Serializable
data class RequestAccount(val login :String, val password :String)

@Serializable
data class Image(val id :Int, val url :String, val date :Long, val lat :Double, val lng :Double)

@Serializable
data class ImageOut(val base64Image :String, val date :Long, val lat: Double, val lng : Double)

@Entity
data class Photo(
    @PrimaryKey
    var id :Int? = null,
    val data :Long,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val image :Bitmap,
    val lng: Double,
    val lat: Double
)

@SuppressLint("MissingPermission")
fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager
    if (connectivityManager != null) {
        val capabilities = connectivityManager.getNetworkCapabilities(
            connectivityManager.activeNetwork
        )
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
            }
        }
    }
    return false
}

@Serializable
data class Comment(val id: Int, val date: Long, val text: String)

class Constants {
    companion object {
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val dateFormat = "dd.MM.yyyy"
        val DBname = "database"
    }
}

class LocalData {
    companion object {
        var token :String? = null
        var userId :Int? = null
        var login :String? = null
        var list :MutableList<Photo>? = null
        var adapter :RecyclerViewAdapter? = null
    }
}