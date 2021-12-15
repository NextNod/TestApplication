package com.example.testapplication.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.example.testapplication.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import java.lang.Exception

class LoginFragment : Fragment() {
    private val client = OkHttpClient()
    val JSON = MediaType.parse("application/json; charset=utf-8")

    fun run(url: String, json :String) :String {
        val request = Request.Builder()
            .post(RequestBody.create(JSON, json))
            .url(url)
            .build()

        client.newCall(request).execute().use { response -> return response.body()!!.string() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_login, container, false)

        root.findViewById<EditText>(R.id.loginLogin).doAfterTextChanged {
            root.findViewById<TextView>(R.id.loginLoginError).text = ""
        }

        root.findViewById<EditText>(R.id.passwordLogin).doAfterTextChanged {
            root.findViewById<TextView>(R.id.passwordLoginError).text = ""
        }

        root.findViewById<Button>(R.id.signUp).setOnClickListener {
            val loginText = root.findViewById<EditText>(R.id.loginLogin).text.toString()
            val passwordText = root.findViewById<EditText>(R.id.passwordLogin).text.toString()
            lateinit var result :String

            Thread {
                result = run(
                    "http://junior.balinasoft.com/api/account/signin",
                    Json.encodeToString(
                        RequestAccount(loginText, passwordText)
                    )
                )
            }.apply {
                start()
                join()
            }

            try {
                val tmp = Json.decodeFromString<Response>(result)
                val accountSession = Json.decodeFromJsonElement<Session>(tmp.data!!)
                LocalData.apply {
                    userId = accountSession.userId
                    login = accountSession.login
                    token = accountSession.token
                }

                val sharedPreferences = requireContext().getSharedPreferences("account", Context.MODE_PRIVATE)
                sharedPreferences.edit().apply {
                    putString("login", accountSession.login)
                    putString("token", accountSession.token)
                    putInt("userId", accountSession.userId)
                    apply()
                }

                val intent = Intent(root.context, MainActivity::class.java)
                startActivity(intent)
            } catch (ex: Exception) {
                val tmp = Json.decodeFromString<Response>(result)
                if(tmp.valid != null) {
                    tmp.valid.forEach {
                        when (it.field) {
                            "login" -> root.findViewById<TextView>(R.id.loginLoginError).text =
                                it.message
                            "password" -> root.findViewById<TextView>(R.id.passwordLoginError).text =
                                it.message
                        }
                    }
                } else {
                    Snackbar.make(root, "Something went wrong(", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        return root
    }
}