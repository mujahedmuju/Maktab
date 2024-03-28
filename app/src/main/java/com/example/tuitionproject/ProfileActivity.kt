package com.example.tuitionproject

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.tuitionproject.databinding.ActivityProfileBinding
import java.net.URLEncoder

class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.closeBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.shareBtn.setOnClickListener {
            intentToWhatsapp()
        }
//        binding.shareBtn.setOnClickListener {
//            val mobileNumber = binding.mobileNoEdtTxt.text?.trim().toString()
//
//            if (mobileNumber.length != 10) {
//                binding.mobileNoEdtTxt.error = "Mobile number must be 10 digits!!"
//            } else {
//                intentToWhatsapp(mobileNumber)
//            }
//        }
    }

    private fun intentToWhatsapp() {
        val message = """
            *Assalamualaikum,*

            It's a pleasure to have you join us for Islamic education.

            Should you have any questions or require assistance, feel free to reach out to us at any time.

            For inquiries, 
            Mohammed Abdul Lateef Ghouri
            *Idara Rauzat ul Ashraf!*
            Contact: +918919133812
            Address: Opp. Centenary Baptist Church, NRR Puram Colony, Site -3, Borabanda, Hyderabad.
            https://maps.app.goo.gl/RxwpkHsoYdrvezcS7
        """.trimIndent()

//        val trimToNumber = "+91${mobileNumber}" // 10 digit number
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(
            "https://wa.me/?text=${
                URLEncoder.encode(
                    message,
                    "UTF-8"
                )
            }"
        )
        startActivity(intent)
//        binding.mobileNoEdtTxt.text?.clear()
    }
}
