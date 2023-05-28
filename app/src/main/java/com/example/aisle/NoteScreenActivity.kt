package com.example.aisle

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class NoteScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notes_screen)

        // Retrieve the auth token from the intent's extras
        val authToken = intent.getStringExtra("authToken") ?: ""
        getProfile(authToken)
    }

    private fun loadScreen(firstName: String, age: Int, photoUrl: String) {
        val tvFirstNameAge = findViewById<TextView>(R.id.tv_first_name_age)
        runOnUiThread {
            tvFirstNameAge.text = "$firstName, $age"

            val imageView = findViewById<ImageView>(R.id.img_profile)

            Glide.with(this)
                .load(photoUrl)
                .error(R.drawable.img_meena)
                .into(imageView)
        }
    }

    private fun getProfile(authToken: String) {
        val url = "https://app.aisle.co/V1/users/test_profile_list"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", authToken)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@NoteScreenActivity, "Request Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.has("invites")) {
                            val invites = jsonResponse.getJSONObject("invites")
                            val profilesArray = invites.getJSONArray("profiles")
                            val firstProfile = profilesArray.getJSONObject(0)

                            val firstName = firstProfile.getJSONObject("general_information")
                                .getString("first_name")
                            val age =
                                firstProfile.getJSONObject("general_information").getInt("age")

                            val photosArray = firstProfile.getJSONArray("photos")
                            val photoObject = photosArray.getJSONObject(0)
                            val imageName = photoObject.getString("photo")

                            loadScreen(firstName, age, imageName)

                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@NoteScreenActivity,
                                    "Profiles not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: JSONException) {
                        runOnUiThread {
                            Toast.makeText(this@NoteScreenActivity, "failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        })
    }
}
