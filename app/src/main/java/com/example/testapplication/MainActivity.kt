package com.example.testapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.ui.main.SectionsPagerAdapter
import com.example.testapplication.databinding.ActivityMainBinding
import com.example.testapplication.ui.main.MainActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("account", Context.MODE_PRIVATE)
        val allData = sharedPreferences.all

        if(allData.size == 3) {
            allData.forEach { (type, data) ->
                LocalData.apply {
                    when (type) {
                        "login" -> login = data as String
                        "token" -> token = data as String
                        "userId" -> userId = data as Int
                    }
                }
            }

            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
    }
}