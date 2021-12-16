package com.example.testapplication.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.testapplication.*
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.*
import kotlinx.serialization.encodeToString
import java.io.ByteArrayOutputStream
import android.view.MenuItem
import com.example.testapplication.EnterActivity
import com.example.testapplication.databinding.ActivityMain2Binding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.decodeFromJsonElement

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMain2Binding
    private val client = OkHttpClient()
    private var getImage = false
    private val REQUEST_CODE = 200
    private var resultImage :Bitmap? = null
    private var snackbar :Snackbar? = null
    private var location :Location? = null

    private val mLocationListener = LocationListener {
        location = it
        if(resultImage != null) {
            saveImage()
        }
    }

    private fun saveImage() {
        val date = Date().date.toLong()
        val tmp = Photo(
            null,
            date,
            resultImage!!,
            location!!.longitude,
            location!!.latitude
        )
        sendPhoto(tmp)

        resultImage = null
        snackbar?.dismiss()
    }

    @SuppressLint("MissingPermission")
    private fun getImage() {
        val mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        mLocationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0L,
            0f,
            mLocationListener
        )
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CODE)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.username).text = LocalData.login

        if(!isOnline(applicationContext)) {
            binding.appBarMain.fab.visibility = View.INVISIBLE
        }

        binding.appBarMain.fab.setOnClickListener {
            getImage = true
            if(isPermissionsAllowed())
                getImage()
            else
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    REQUEST_CODE
                )
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(getImage) {
            getImage = false
            if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE && data != null) {
                if(snackbar == null) {
                    snackbar = Snackbar.make(binding.root, "Please wait...", Snackbar.LENGTH_INDEFINITE)
                    snackbar!!.show()
                }
                val matrix = Matrix()
                matrix.postRotate(90F)
                val rawImage = data.extras!!.get("data") as Bitmap
                resultImage = Bitmap.createBitmap(
                    rawImage,
                    0, 0,
                    rawImage.width,
                    rawImage.height,
                    matrix,
                    true
                )

                if(location != null) {
                    saveImage()
                }
            } else {
                Snackbar.make(binding.root, "I didn't get the data(", Snackbar.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun sendPhoto(photo: Photo) {
        lateinit var result :Response
        Thread{
            val byteArrayOutputStream = ByteArrayOutputStream()
            photo.image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

            val image = Base64.getEncoder().encodeToString(byteArray)
            val json = ImageOut(image, photo.data, photo.lat, photo.lng)
            val request = Request.Builder()
                .header("Access-Token", LocalData.token!!)
                .post(RequestBody.create(Constants.JSON, Json.encodeToString(json)))
                .url("http://junior.balinasoft.com/api/image")
                .build()

            client.newCall(request).execute().use { response ->
                result = Json.decodeFromString(response.body()!!.string())
            }
        }.apply {
            start()
            join()
        }

        if(result.status == 200) {
            val resultImage = Json.decodeFromJsonElement<Image>(result.data!!)
            photo.id = resultImage.id

            val db = Room.databaseBuilder(this, AppDatabase::class.java, "database")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries().build().dao()

            db!!.insert(photo)
            LocalData.apply {
                list!!.add(photo)
                adapter!!.notifyDataSetChanged()
            }
        } else {
            Snackbar.make(binding.root, "Something went wrong((", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.exitAction) {
            val sharedPreferences = getSharedPreferences("account", 0)
            sharedPreferences.edit().apply {
                clear()
                apply()
            }
            startActivity(Intent(applicationContext, EnterActivity::class.java))
            finish()
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED
                ) {
                    getImage()
                } else {
                    Snackbar.make(
                        binding.root,
                        "You don't want me to take pictures " +
                                String(Character.toChars(0x1F97A)) +
                                String(Character.toChars(0x1F449)) +
                                String(Character.toChars(0x1F448)),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    private fun isPermissionsAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}