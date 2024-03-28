package com.example.tuitionproject

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import carbon.widget.Button
import com.example.tuitionproject.adapter.UpcomingPayAdapter
import com.example.tuitionproject.databinding.ActivityUpcomingPayBinding
import com.example.tuitionproject.databinding.CustomDialogBinding
import com.example.tuitionproject.model.Student
import com.example.tuitionproject.utils.ProgressDialogHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpcomingPayActivity : AppCompatActivity(), UpcomingPayAdapter.ItemClickListener {

    private lateinit var binding: ActivityUpcomingPayBinding
    private val studentPaymentList = mutableListOf<Student>()
    private lateinit var adapter: UpcomingPayAdapter
    private val progressDialog by lazy { ProgressDialogHelper(this@UpcomingPayActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpcomingPayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpRecyclerView()
        setUpListeners()

        fetchStudentPaymentDetails()
    }

    private fun setUpRecyclerView() {
        adapter = UpcomingPayAdapter(itemClickListener = this)
        binding.upcomingPayRV.layoutManager = LinearLayoutManager(this@UpcomingPayActivity)
        binding.upcomingPayRV.adapter = adapter
    }

    private fun setUpListeners() {
        binding.upcomingDuesBtn.setOnClickListener {
            loadUpcomingDuesList()
            updateButtonColors(binding.upcomingDuesBtn, true)
            updateButtonColors(binding.duesBtn, false)
        }

        binding.duesBtn.setOnClickListener {
            loadDueList()
            updateButtonColors(binding.upcomingDuesBtn, false)
            updateButtonColors(binding.duesBtn, true)
        }

        binding.closeBtn.setOnClickListener {
            startActivity(Intent(this@UpcomingPayActivity, HomeActivity::class.java))
            finish()
        }
    }

    private fun updateButtonColors(button: Button, isSelected: Boolean) {
        val colorResId = if (isSelected) carbon.R.color.carbon_amber_50 else R.color.white
        button.backgroundTintList =
            ContextCompat.getColorStateList(this@UpcomingPayActivity, colorResId)
    }

    private fun loadUpcomingDuesList() {
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

        val filteredList = studentPaymentList.filter { it ->
            val paymentDueDate =
                it.paymentDueDate?.takeIf { it.isNotEmpty() }?.let { dateFormat.parse(it) }
            paymentDueDate != null && currentDate <= paymentDueDate
        }
        adapter.updateData(filteredList)
        handleDataVisibility()
    }

    private fun loadDueList() {
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

        val filteredList = studentPaymentList.filter { it ->
            val paymentDueDate =
                it.paymentDueDate?.takeIf { it.isNotEmpty() }?.let { dateFormat.parse(it) }
            paymentDueDate != null && currentDate > paymentDueDate
        }
        adapter.updateData(filteredList)
        handleDataVisibility()
    }

    private fun handleDataVisibility() {
        progressDialog.hideProgressDialog()
        binding.noDataTxtView.visibility = if (adapter.itemCount == 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun handleFetchError(exception: Exception) {
        progressDialog.hideProgressDialog()
        Toast.makeText(
            this@UpcomingPayActivity,
            "Error : $exception",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun fetchStudentPaymentDetails() {
        progressDialog.showProgressDialog()
        val db = FirebaseFirestore.getInstance()

        db.collection("Students")
            .orderBy("paymentDueDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val student = document.toObject(Student::class.java)
                    studentPaymentList.add(student)
                }
                loadUpcomingDuesList()
                handleDataVisibility()
            }
            .addOnFailureListener { exception ->
                handleFetchError(exception)
            }
    }

    override fun onItemClick(student: Student) {
        val dialogBinding = CustomDialogBinding.inflate(layoutInflater)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogBinding.root)

        val dialog = dialogBuilder.create()

        dialogBinding.sendMsgBtn.setOnClickListener {

            val whatsappContent = """     
*REMINDER!!!*
Assalamualaikum ${student.name}!
This is a reminder to kindly settle your monthly tuition fee at *Idara Rauzat ul Ashraf*. 
The due date for payment is *[${student.paymentDueDate}]*.

JazakAllah Khair.
""".trimIndent()
            val trimToNumber = "+91${student.mobileNo}"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(
                "https://wa.me/$trimToNumber/?text=${
                    URLEncoder.encode(
                        whatsappContent,
                        "UTF-8"
                    )}"
            )
            startActivity(intent)
            dialog.dismiss()
        }
        dialogBinding.paymentScreenBtn.setOnClickListener {
            Toast.makeText(this, "Payments In Progress!!",Toast.LENGTH_SHORT).show()
//            val intent = Intent(this@UpcomingPayActivity, PaymentDetailsActivity::class.java)
//            intent.putExtra("studentData", student)
//            intent.putExtra("studentId", student.studentId)
//            startActivity(intent)
//            finish()
            dialog.dismiss()
        }

        dialog.show()
    }

}