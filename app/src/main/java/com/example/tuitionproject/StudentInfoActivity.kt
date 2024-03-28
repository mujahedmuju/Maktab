package com.example.tuitionproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tuitionproject.databinding.ActivityStudentInfoBinding
import com.example.tuitionproject.utils.FireStoreHelper
import com.example.tuitionproject.model.Student


class StudentInfoActivity : AppCompatActivity() {
    lateinit var binding: ActivityStudentInfoBinding
    private val callIntent = Intent(Intent.ACTION_CALL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val studentData = intent.getParcelableExtra<Student>("studentData")
        val studentId = intent.getStringExtra("studentId")


        if (studentData != null) {
            binding.nameTv.text = studentData.name.toString()
            binding.fatherNameTv.text = studentData.fatherName.toString()
            binding.mobileNoTv.text = studentData.mobileNo.toString()
            binding.dobTv.text = studentData.dob.toString()
            binding.dojTv.text = studentData.doj.toString()
            binding.courseTv.text = studentData.course.toString()
            binding.timeTv.text = studentData.timing.toString()
            binding.addressTv.text = studentData.address.toString()
        }

        binding.backBtn.setOnClickListener {
            val intent = Intent(this@StudentInfoActivity, StudentDetailsActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.callBtn.setOnClickListener {
            val phoneNumber = studentData?.mobileNo.toString()
            callIntent.data = Uri.parse("tel:$phoneNumber")

            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    MY_PERMISSIONS_REQUEST_CALL_PHONE
                )
            } else {
                // Permission already granted, check if there's an activity to handle the call intent
                if (callIntent.resolveActivity(packageManager) != null) {
                    startActivity(callIntent)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "No app found to handle the call.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

        binding.whatsappBtn.setOnClickListener {
            if (studentData != null) {
                try {
                    val autoMsg = "Assalamualaikum"
                    val trimToNumber = "+91${studentData.mobileNo}"
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://wa.me/$trimToNumber/?text=$autoMsg")
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

        binding.editProfileBtn.setOnClickListener {
            val intent = Intent(applicationContext, AdmissionActivity::class.java)
            intent.putExtra("update_title", "Update Student Details")
            if (studentData != null) {
                intent.putExtra("studentId", studentId.toString())
                intent.putExtra("update_name", studentData.name.toString())
                intent.putExtra("update_fatherName", studentData.fatherName.toString())
                intent.putExtra("update_mobileNo", studentData.mobileNo.toString())
                intent.putExtra("update_dob", studentData.dob.toString())
                intent.putExtra("update_time", studentData.timing.toString())
                intent.putExtra("update_course", studentData.course.toString())
                intent.putExtra("update_address", studentData.address.toString())
            }
            startActivity(intent)
        }

        binding.paymentDetailsBtn.setOnClickListener {
            if (studentData != null) {
                val intent = Intent(this@StudentInfoActivity, PaymentDetailsActivity::class.java)
                intent.putExtra("studentData", studentData)
                intent.putExtra("studentId", studentId.toString())
                startActivity(intent)
            }
        }

        binding.deleteBtn.setOnClickListener {
            onClickDeleteAccount()
        }
    }

    private fun onClickDeleteAccount() {
        val studentId = intent.getStringExtra("studentId")

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to Delete Student?")
            .setPositiveButton("Yes") { _, _ ->
                if (studentId != null) {
                    // Delete the user
                    FireStoreHelper.deleteDocument("Students", studentId)
                        .addOnSuccessListener {
                            // Document deleted successfully
                            Toast.makeText(
                                applicationContext,
                                "User deleted successfully!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Navigate to the home activity
                            val intent =
                                Intent(this@StudentInfoActivity, StudentDetailsActivity::class.java)
                            startActivity(intent)
                            finish() // Close the current activity
                        }
                        .addOnFailureListener {
                            // Handle the failure
                            Toast.makeText(
                                applicationContext,
                                "Error deleting user: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }

            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CALL_PHONE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, make the call
                    startActivity(callIntent)
                } else {
                    // Permission denied, show a message or handle accordingly
                    Toast.makeText(
                        applicationContext,
                        "Permission denied. Cannot make a call.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_CALL_PHONE = 101
    }

    override fun onResume() {
        super.onResume()

        // Get the studentId from the intent
        val studentId = intent.getStringExtra("studentId")

        if (studentId != null) {
            // Fetch the updated data from Firestore
            FireStoreHelper.getDocumentById(
                collection = "Students",
                documentId = studentId
            )
                .addOnSuccessListener { documentSnapshot ->
                    documentSnapshot.data.let {
                        val studentData = documentSnapshot.toObject(Student::class.java)
                        // Check if the conversion was successful
                        if (studentData != null) {
                            // Update UI with the new data
                            updateUI(studentData)
                        } else {
                            Toast.makeText(
                                baseContext,
                                "Failed to modify document",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .addOnFailureListener {
                    // Handle the failure
                }
        }
    }

    private fun updateUI(studentData: Student) {
        // Update your UI with the new data
        binding.nameTv.text = studentData.name.toString()
        binding.fatherNameTv.text = studentData.fatherName.toString()
        binding.mobileNoTv.text = studentData.mobileNo.toString()
        binding.dobTv.text = studentData.dob.toString()
        binding.dojTv.text = studentData.doj.toString()
        binding.courseTv.text = studentData.course.toString()
        binding.timeTv.text = studentData.timing.toString()
        binding.addressTv.text = studentData.address.toString()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, StudentDetailsActivity::class.java))
        finish()
    }

}