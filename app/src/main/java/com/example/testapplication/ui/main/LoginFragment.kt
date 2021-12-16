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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.lang.Exception
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginFragment : Fragment() {
    fun run(account: RequestAccount) :ResponseRetrofit? {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://junior.balinasoft.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: Login = retrofit.create(Login::class.java)
        val rawResult = service.getSession(account).execute()
        return rawResult.body()

        /*val request = Request.Builder()
            .post(RequestBody.create(JSON, json))
            .url(url)
            .build()

        client.newCall(request).execute().use { response -> return response.body()!!.string() }*/
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
            var result :ResponseRetrofit? = null

            Thread {
                result = run(RequestAccount(loginText, passwordText))
            }.apply {
                start()
                join()
            }

            if(result != null) {
                try {
                    LocalData.apply {
                        userId = result!!.data.userId
                        login = result!!.data.login
                        token = result!!.data.token
                    }

                    val sharedPreferences =
                        requireContext().getSharedPreferences("account", Context.MODE_PRIVATE)
                    sharedPreferences.edit().apply {
                        putString("login", result!!.data.login)
                        putString("token", result!!.data.token)
                        putInt("userId", result!!.data.userId)
                        apply()
                    }

                    val intent = Intent(root.context, MainActivity::class.java)
                    startActivity(intent)
                } catch (ex: Exception) {
                    /*val tmp = Json.decodeFromString<Response>(result)
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
                */
                }
            } else {
                Snackbar.make(root, "Something went wrong(", Snackbar.LENGTH_LONG).show()
            }
        }

        return root
    }
}