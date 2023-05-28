package com.example.aisle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var countryCodeEditText: AppCompatEditText
    private lateinit var mobileNumberEditText: AppCompatEditText
    private lateinit var button: Button
    private val client = OkHttpClient()
    private var isButtonClickable = true
    private var isPhoneNumberValid = false
    private var mobileNumber = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countryCodeEditText = findViewById(R.id.et_country_code)
        mobileNumberEditText = findViewById(R.id.et_number)
        button = findViewById(R.id.btn_continue)

        mobileNumberEditText.inputType = InputType.TYPE_CLASS_NUMBER
        mobileNumberEditText.filters = arrayOf(InputFilter.LengthFilter(10))
        countryCodeEditText.inputType = InputType.TYPE_CLASS_PHONE
        countryCodeEditText.filters = arrayOf(InputFilter.LengthFilter(4))

        mobileNumberEditText.addTextChangedListener(phoneNumberTextWatcher)

        button.setOnClickListener {
            if (isButtonClickable) {
                isButtonClickable = false
                val countryCode = countryCodeEditText.text.toString()
                val mobileNumber = mobileNumberEditText.text.toString()

                val phoneNumber = "$countryCode$mobileNumber"

                if (isInputValid(countryCode, mobileNumber)) {
                    validatePhoneNumber(phoneNumber)
                } else {
                    Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show()
                    isButtonClickable = true // Enable the button again
                }
            }
        }
    }

    private fun isInputValid(countryCode: String, mobileNumber: String): Boolean {
        // Validate the phone number format using a regular expression
        val phoneNumberRegex = Regex("^\\d{10}\$")
        val isValid = countryCode.isNotEmpty() && phoneNumberRegex.matches(mobileNumber)
        if (isValid) {
            isButtonClickable = true // Enable the button
        }
        return isValid
    }

    private val phoneNumberTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val countryCode = countryCodeEditText.text.toString()
             mobileNumber = s.toString()
            isPhoneNumberValid = isInputValid(countryCode, mobileNumber)
        }
    }

    private fun validatePhoneNumber(number: String) {
        val url = "https://app.aisle.co/V1/users/phone_number_login"
        val requestBody = FormBody.Builder()
            .add("number", number)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle API call failure
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    Toast.makeText(applicationContext, "Failure occurred", Toast.LENGTH_SHORT).show()
                    isButtonClickable = true // Enable the button again
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // API call was successful
                    if (number == "+919876543212") {
                        // Phone number validation succeeded
                        // Open OTPVerification and pass mobile number
                        val intent = Intent(this@MainActivity, OtpVerificationActivity::class.java)
                        intent.putExtra("phoneNumber", number)
                        startActivity(intent)
                    } else {
                        // Phone number validation failed
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            Toast.makeText(this@MainActivity, "Invalid number", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    // Handle error case
                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
                        Toast.makeText(this@MainActivity, "API call failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                isButtonClickable = true // Enable the button again
            }
        })
    }
}
