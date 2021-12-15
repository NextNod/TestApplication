package com.example.testapplication.ui.main

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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.lang.Exception

class RegisterFragment : Fragment() {
    private val client = OkHttpClient()
    val JSON = MediaType.parse("application/json; charset=utf-8")

    private fun run(url: String, json :String) :String {
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
        val root = inflater.inflate(R.layout.fragment_register, container, false)

        root.findViewById<EditText>(R.id.loginReg).doAfterTextChanged {
            root.findViewById<TextView>(R.id.loginError).text = ""
        }

        root.findViewById<EditText>(R.id.passwordReg).doAfterTextChanged {
            root.findViewById<TextView>(R.id.passwordError).text = ""
        }

        root.findViewById<EditText>(R.id.passwordRepeat).doAfterTextChanged {
            root.findViewById<TextView>(R.id.repeatPasswordError).text = ""
        }

        root.findViewById<Button>(R.id.signUp).setOnClickListener {
            val login = root.findViewById<EditText>(R.id.loginReg).text.toString()
            val password = root.findViewById<EditText>(R.id.passwordReg).text.toString()
            val passwordRepeat = root.findViewById<EditText>(R.id.passwordRepeat).text.toString()

            if(password == passwordRepeat) {
                lateinit var result: String
                Thread {
                    result = run(
                        "http://junior.balinasoft.com/api/account/signup",
                        Json.encodeToString(RequestAccount(login, password))
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
                        LocalData.login = accountSession.login
                        token = accountSession.token
                    }

                    val intent = Intent(root.context, MainActivity::class.java)
                    startActivity(intent)
                } catch (ex: Exception) {
                val tmp = Json.decodeFromString<Response>(result)
                    if(tmp.valid != null) {
                        tmp.valid.forEach {
                            when (it.field) {
                                "login" -> root.findViewById<TextView>(R.id.loginError).text =
                                    it.message
                                "password" -> root.findViewById<TextView>(R.id.passwordError).text =
                                    it.message
                            }
                        }
                    } else {
                        Snackbar.make(root, "Something went wrong(", Snackbar.LENGTH_SHORT).show()
                    }
                }
            } else { root.findViewById<TextView>(R.id.repeatPasswordError).text = "Passwords don't match" }
        }

        return root
    }
}