package com.example.tuitionproject

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tuitionproject.adapter.StudentAdapter
import com.example.tuitionproject.databinding.ActivityStudentDetailsBinding
import com.example.tuitionproject.model.Student
import com.example.tuitionproject.utils.FireStoreHelper
import com.example.tuitionproject.utils.GestureManager
import com.example.tuitionproject.utils.ProgressDialogHelper


class StudentDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentDetailsBinding
    private val studentList = mutableListOf<Student>()
    private lateinit var adapter: StudentAdapter
    private val progressDialog by lazy { ProgressDialogHelper(this@StudentDetailsActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displayStudentsInRecyclerView(studentList)

        setupListeners()

//        adapter.updateData(studentList)
        adapter = StudentAdapter(applicationContext, studentList)

        val searchView = binding.stdSearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    // If empty, show the entire list
                    adapter.filter.filter(null)
                    displayStudentsInRecyclerView(studentList)
                } else {
                    // If not empty, filter the RecyclerView based on the search query
                    adapter.filter.filter(newText)

                    // Check if the filtered list is empty
                    if (adapter.itemCount == 0) {
                        // If no matches found, display a toast message
                        Toast.makeText(applicationContext, "Data not found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                return true
            }
        })
    }

    private fun setupListeners() {
        binding.admissionBtn.setOnClickListener {
            startActivity(Intent(this@StudentDetailsActivity, AdmissionActivity::class.java))
        }

        binding.closeBtn.setOnClickListener {
            startActivity(Intent(this@StudentDetailsActivity, HomeActivity::class.java))
            finish()
        }
    }

    private fun getDataFromFireStore() {
        progressDialog.showProgressDialog()
        studentList.clear()

        FireStoreHelper.getDocuments("Students")
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val student = document.toObject(Student::class.java)
                    if (student != null) {
                        student.studentId = document.id
                        studentList.add(student)
                        progressDialog.hideProgressDialog()
                    } else {
                        Toast.makeText(this, "No Data found", Toast.LENGTH_SHORT).show()
                        progressDialog.hideProgressDialog()
                    }
                }
                displayStudentsInRecyclerView(studentList)
            }.addOnFailureListener { e ->
                progressDialog.hideProgressDialog()
                Toast.makeText(this, "Error : " + e.message, Toast.LENGTH_SHORT).show()

            }
    }

    private fun displayStudentsInRecyclerView(studentList: MutableList<Student>) {
//        studentList.sortBy { it.name?.firstOrNull()?.uppercaseChar() }
        studentList.sortBy { it.name?.firstOrNull()?.uppercase() }

        binding.stdRecyclerView.setHasFixedSize(true)
        binding.stdRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        adapter = StudentAdapter(applicationContext, studentList)
//        adapter.updateData(studentList)
        binding.stdRecyclerView.adapter = adapter

        if (studentList.isEmpty()) {
            progressDialog.hideProgressDialog()
            binding.noDataTxtView.visibility = View.VISIBLE
        } else {
            binding.noDataTxtView.visibility = View.GONE
        }
        // Set up item click listener
        adapter.setOnItemClickListener { selectedStudent ->
            progressDialog.showProgressDialog()
            FireStoreHelper.getDocumentById(
                collection = "Students",
                documentId = selectedStudent.studentId.toString()
            ).addOnSuccessListener { documentSnapshot ->
                documentSnapshot.data.let {
                    val studentData = documentSnapshot.toObject(Student::class.java)
                    if (studentData != null) {
                        val intent = Intent(this, StudentInfoActivity::class.java)
                        intent.putExtra("studentId", selectedStudent.studentId)
                        intent.putExtra("studentData", studentData)
                        progressDialog.hideProgressDialog()
                        startActivity(intent)
                    } else {
                        Log.d("MyError", "Failed to convert document to StudentClass")
                    }
                }
            }
                .addOnFailureListener { e ->
                    progressDialog.showProgressDialog()
                    Toast.makeText(this, "Error : " + e.message, Toast.LENGTH_SHORT).show()
                }
        }

        val leftCallback = object : GestureManager.SwipeCallbackLeft {
            override fun onLeftSwipe(position: Int) {
                val selectedStudent = adapter.studentList[position]
                val intent = Intent(this@StudentDetailsActivity, PaymentDetailsActivity::class.java)
                intent.putExtra("studentId", selectedStudent.studentId)
                intent.putExtra("studentData", selectedStudent)
                startActivity(intent)
            }
        }

        val recyclerAdapterSwipeGestures = GestureManager(leftCallback)
        recyclerAdapterSwipeGestures.setBackgroundColorLeft(
            ColorDrawable(
                ContextCompat.getColor(
                    this@StudentDetailsActivity,
                    R.color.secondaryColor
                )
            )

        )
        recyclerAdapterSwipeGestures.setTextLeft("Payments")
        recyclerAdapterSwipeGestures.setTextSize(50)
        recyclerAdapterSwipeGestures.setTextColor(ContextCompat.getColor(this, R.color.white))


        val itemTouchHelper = ItemTouchHelper(recyclerAdapterSwipeGestures)
        itemTouchHelper.attachToRecyclerView(binding.stdRecyclerView)

    }


    override fun onResume() {
        super.onResume()
        studentList.clear()
        getDataFromFireStore()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

}

