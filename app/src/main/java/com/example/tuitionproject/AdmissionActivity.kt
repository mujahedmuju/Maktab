package com.example.tuitionproject

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tuitionproject.databinding.ActivityAdmissionBinding
import com.example.tuitionproject.utils.FireStoreHelper
import com.example.tuitionproject.model.Student
import com.example.tuitionproject.utils.ProgressDialogHelper
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class AdmissionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdmissionBinding
    private val progressDialog by lazy { ProgressDialogHelper(this@AdmissionActivity) }
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdmissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleIntent()
        setupListeners()
        setupDropDowns()
    }

    private fun handleIntent() {
        val intent = intent
        if (intent != null && intent.hasExtra("update_title")) {
            binding.titleTxt.text = getString(R.string.update_student_details)
            intent.extras?.let { bundle ->
                binding.nameEdtTxt.setText(bundle.getString("update_name", ""))
                binding.fatherNameEdtTxt.setText(bundle.getString("update_fatherName", ""))
                binding.mobileNoEdtTxt.setText(bundle.getString("update_mobileNo", ""))
                binding.dobEdtTxt.setText(bundle.getString("update_dob", ""))
                binding.timeEdtTxt.setText(bundle.getString("update_time", ""))
                binding.courseEdtTxt.setText(bundle.getString("update_course", ""))
                binding.addressEdtTxt.setText(bundle.getString("update_address", ""))
            }
        }
    }

    private fun setupListeners() {
        binding.closeBtn.setOnClickListener {
            navigateToStudentDetailsActivity()
        }

        binding.dobEdtTxt.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDatePicker()
            }
        }

        binding.saveBtn.setOnClickListener { onClickSaveBtn() }
    }

    private fun setupDropDowns() {
        binding.timeEdtTxt.setAdapter(
            ArrayAdapter(
                this,
                R.layout.drop_down_item,
                arrayOf("5:30pm to 7:00pm", "6:30pm to 8:00pm", "7:30pm to 9:00pm")
            )
        )

        binding.courseEdtTxt.setAdapter(
            ArrayAdapter(
                this,
                R.layout.drop_down_item,
                arrayOf(getString(R.string.nooraniquida_deeniyat), getString(R.string.quran_deeniyat))
            )
        )
    }

    private fun navigateToStudentDetailsActivity() {
        startActivity(Intent(this@AdmissionActivity, StudentDetailsActivity::class.java))
        finish()
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            R.style.my_dialog_theme,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private val dateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(year, monthOfYear, dayOfMonth)
            updateEditTextDate()
        }

    private fun updateEditTextDate() {
        val dateFormat = "dd-MMM-yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        binding.dobEdtTxt.setText(sdf.format(calendar.time))
    }

    private fun onClickSaveBtn() {
        val name = binding.nameEdtTxt.text.toString().trim()
        val fatherName = binding.fatherNameEdtTxt.text.toString().trim()
        val mobileNo = binding.mobileNoEdtTxt.text.toString().trim()
        val dob = binding.dobEdtTxt.text.toString().trim()
        val timing = binding.timeEdtTxt.text.toString().trim()
        val course = binding.courseEdtTxt.text.toString().trim()
        val address = binding.addressEdtTxt.text.toString().trim()

        if (validateInput(name, fatherName, mobileNo, dob, timing, course)) {
            if (binding.titleTxt.text == "Update Student Details") {
                updateStudentDetails(name, fatherName, mobileNo, dob, timing, course, address)
            } else {
                saveNewStudentDetails(name, fatherName, mobileNo, dob, timing, course, address)
            }
        }
    }

    private fun validateInput(
        name: String,
        fatherName: String,
        mobileNo: String,
        dob: String,
        timing: String,
        course: String
    ): Boolean {
        if (name.isEmpty()) {
            binding.nameEdtTxt.error = "Enter Student Name"
            return false
        }
        if (fatherName.isEmpty()) {
            binding.fatherNameEdtTxt.error = "Enter Father Name"
            return false
        }
        if (mobileNo.isEmpty()) {
            binding.mobileNoEdtTxt.error = "Enter Whatsapp Mobile Number"
            return false
        }
        if (dob.isEmpty()) {
            binding.dobEdtTxt.error = "Enter Date Of Birth"
            return false
        }
        if (timing.isEmpty()) {
            binding.timeEdtTxt.error = "Enter Timings"
            return false
        }
        if (course.isEmpty()) {
            binding.courseEdtTxt.error = "Enter Course"
            return false
        }
        return true
    }

    private fun updateStudentDetails(
        name: String,
        fatherName: String,
        mobileNo: String,
        dob: String,
        timing: String,
        course: String,
        address: String
    ) {
        progressDialog.showProgressDialog()
        val updatedStudentData = mapOf(
            "name" to name,
            "fatherName" to fatherName,
            "mobileNo" to mobileNo,
            "dob" to dob,
            "timing" to timing,
            "course" to course,
            "address" to address
        )
        val studentId = intent.getStringExtra("studentId")
        studentId?.let {
            FireStoreHelper.updateDocument("Students", it, updatedStudentData)
                .addOnSuccessListener {
                    Toast.makeText(
                        applicationContext,
                        "Data updated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    clearFields()
                    progressDialog.hideProgressDialog()
                    finish()
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        applicationContext,
                        "Error updating data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressDialog.hideProgressDialog()
                }
        }
    }

    private fun saveNewStudentDetails(
        name: String,
        fatherName: String,
        mobileNo: String,
        dob: String,
        timing: String,
        course: String,
        address: String
    ) {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        val admitDate = currentDate.format(formatter)

        progressDialog.showProgressDialog()

        val newStudent =
            Student(name, fatherName, mobileNo, dob, admitDate, timing, course, address)

        FireStoreHelper.addDocument("Students", newStudent)
            .addOnSuccessListener {
                Toast.makeText(
                    applicationContext,
                    "Data saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                clearFields()
                progressDialog.hideProgressDialog()
                intentToWhatsapp(name, fatherName, dob, course, timing, admitDate, mobileNo,address)
            }.addOnFailureListener { e ->
                Toast.makeText(
                    applicationContext,
                    "Error saving data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog.hideProgressDialog()
            }
    }

    private fun intentToWhatsapp(
        name: String,
        fatherName: String,
        dob: String,
        course: String,
        timing: String,
        admitDate: String,
        mobileNo: String,
        address: String
    ) {
        val whatsappContent = """
*Assalamualaikum, $name!*

Welcome to *Idara Rauzat ul Ashraf!* We're pleased to have you with us for Islamic education.

For any questions or need assistance, contact: +918919133812

Student Details:
- Father's Name: $fatherName
- DOB: $dob
- Course: $course
- Timings: $timing
- Admission Date: $admitDate
- Address: $address

Best regards,
*Mohammed Abdul Lateef Ghouri*
Idara Rauzat-ul-Ashraf
    """.trimIndent()
        val trimToNumber = "+91${mobileNo}" // 10 digit number
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(
            "https://wa.me/$trimToNumber/?text=${
                URLEncoder.encode(
                    whatsappContent,
                    "UTF-8"
                )
            }"
        )
        startActivity(intent)
        finish()
    }

    private fun clearFields() {
        binding.nameEdtTxt.text?.clear()
        binding.fatherNameEdtTxt.text?.clear()
        binding.mobileNoEdtTxt.text?.clear()
        binding.dobEdtTxt.text?.clear()
        binding.courseEdtTxt.text?.clear()
        binding.timeEdtTxt.text?.clear()
        binding.addressEdtTxt.text?.clear()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToStudentDetailsActivity()
    }
}

//*Assalamualaikum, $name!*
//Thanks for joining *Idara Rauzat ul Ashraf.* We are delighted to welcome you to our learning Institute!
//We look forward to embarking on this enriching islamic education journey together.
//If you have any questions or need assistance, please don't hesitate to contact us +918919133812
//
//Father's Name: $fatherName
//Date of Birth: $dob
//Course: $course
//Timings: $timing
//Admission Date: $admitDate
//
//Best regards,
//*Mohammed Abdul Lateef Ghouri*
//"Idara Rauzat ul Ashraf"



