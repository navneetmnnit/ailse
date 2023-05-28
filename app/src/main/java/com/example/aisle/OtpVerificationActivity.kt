package com.example.aisle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.widget.AppCompatEditText
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var phoneNumber: String
    private lateinit var otpTimer: CountDownTimer
    private lateinit var phoneNumberTextView: TextView
    private lateinit var otpEditText: AppCompatEditText
    private lateinit var continueButton: Button
    private var isButtonClickable = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otp_verification_screen)

        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        phoneNumberTextView = findViewById(R.id.tv_entered_number)
        phoneNumberTextView.text = phoneNumber
        otpEditText = findViewById(R.id.etv_otp)
        continueButton = findViewById(R.id.btn_continue)

        // Start the 1-minute timer
        startOtpTimer()

        continueButton.setOnClickListener {
            if (isButtonClickable) {
                isButtonClickable = false
                val enteredOtp = otpEditText.text.toString()
                verifyOTP(enteredOtp)
            }
        }
    }

    private fun startOtpTimer() {
        val timerTextView = findViewById<TextView>(R.id.tv_timer)
        // Start the 1-minute timer
        otpTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val secondsRemaining = seconds % 60

                val timeString = String.format("%02d:%02d", minutes, secondsRemaining)
                timerTextView.text = timeString
            }

            override fun onFinish() {
                // Timer finished, handle OTP verification timeout
                Toast.makeText(
                    this@OtpVerificationActivity,
                    "Time out Re-Enter Number",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this@OtpVerificationActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }.start()
    }

    private fun verifyOTP(otp: String) {
        val url = "https://app.aisle.co/V1/users/verify_otp"
        val requestBody = FormBody.Builder()
            .add("number", phoneNumber)
            .add("otp", otp)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle API call failure
                Toast.makeText(this@OtpVerificationActivity, "Request Failed", Toast.LENGTH_SHORT)
                    .show()
                otpTimer.cancel()
                isButtonClickable = true
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                // Process the OTP verification API response here
                if (response.isSuccessful){
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val authToken = jsonResponse.optString("token")
                                if (otp == "1234") {
                                    val intent =
                                        Intent(
                                            this@OtpVerificationActivity,
                                            NoteScreenActivity::class.java
                                        )
                                    intent.putExtra("authToken", authToken)
                                    startActivity(intent)
                                    otpTimer.cancel()
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@OtpVerificationActivity,
                                            "Incorrect Otp re enter",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isButtonClickable = true
                                    }
                                }
                        } catch (e: JSONException) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(
                                this@OtpVerificationActivity,
                                "Error parsing JSON response",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    // OTP verification failed
                    runOnUiThread {
                        Toast.makeText(
                            this@OtpVerificationActivity,
                            "OTP verification failed",
                            Toast.LENGTH_SHORT
                        ).show()
                        isButtonClickable = true
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the timer when the activity is destroyed
        otpTimer.cancel()
    }
}