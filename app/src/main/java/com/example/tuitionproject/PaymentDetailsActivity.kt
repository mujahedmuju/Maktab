package com.example.tuitionproject

import android.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tuitionproject.adapter.PaymentAdapter
import com.example.tuitionproject.databinding.ActivityPaymentDetailsBinding
import com.example.tuitionproject.databinding.BottomSheetBinding
import com.example.tuitionproject.model.PaymentData
import com.example.tuitionproject.model.Student
import com.example.tuitionproject.utils.FireStoreHelper.addPayment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class PaymentDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentDetailsBinding
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheet: BottomSheetBinding
    private lateinit var adapter: PaymentAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        val studentData = intent.getParcelableExtra<Student>("studentData")
        val studentId = intent.getStringExtra("studentId")

        getPaymentDetailsAndUpdateAdapter(studentId.toString())

        studentData?.let {
            binding.stdNameTv.text = it.name.toString()
            binding.fatherNameTv.text = "C/O " + it.fatherName.toString()
            binding.mobileNoTv.text = "Mobile No : " + it.mobileNo.toString()
            binding.dueDateTv.text = "Due Date : " + it.paymentDueDate
        }

        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheet = BottomSheetBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomSheet.root)

        setSpinner(studentData)
    }

    private fun setupListeners() {
        binding.newPaymentBtn.setOnClickListener {
            bottomSheetDialog.show()
        }

        bottomSheet.closeBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheet.saveBtn.setOnClickListener {
            val studentData = intent.getParcelableExtra<Student>("studentData")
            val studentId = intent.getStringExtra("studentId")
            studentData?.let {
                savePayment(it, studentId)
            }
        }

        binding.backBtn.setOnClickListener {
            startActivity(Intent(this@PaymentDetailsActivity, StudentDetailsActivity::class.java))
            finish()
        }
    }

    private fun setSpinner(studentData: Student?) {
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            com.example.tuitionproject.R.array.installments_options,
            R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        bottomSheet.installmentSpinner.adapter = adapter

        bottomSheet.installmentSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val dateString = studentData?.doj.toString()
                    val pattern = "dd-MMM-yyyy"
                    val localDate = stringToLocalDate(dateString, pattern)
                    calculateDueDate(localDate)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }
    }

    private fun savePayment(studentData: Student, studentId: String?) {
        val totalAmt = bottomSheet.totalAmtEditTxt.text.toString().trim()
        val balanceAmt = bottomSheet.balanceAmtEditTxt.text.toString().trim()
        val remarkTxt = bottomSheet.remarkEditTxt.text.toString().trim()

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        val paidDate = currentDate.format(formatter)

        if (totalAmt.isEmpty()) {
            bottomSheet.totalAmtEditTxt.error = "Enter Total Amount"
            return
        } else if (balanceAmt.isEmpty()) {
            bottomSheet.balanceAmtEditTxt.error = "Enter Balance Amount"
            return
        }
        val dateString = studentData.doj.toString()
        val pattern = "dd-MMM-yyyy"
        val localDate = stringToLocalDate(dateString, pattern)
        val calculatedDueDate: String = calculateDueDate(localDate)

        studentData.paymentDueDate = calculatedDueDate // Update the payment due date

        val paymentData = PaymentData(
            paidDate = paidDate,
            totalAmountPaid = totalAmt,
            balanceAmount = balanceAmt,
            dueDate = calculatedDueDate,
            remarks = remarkTxt
        )

        addPayment(studentId.toString(), paymentData)
            .addOnSuccessListener { documentReference ->
                // Update student document in Fire store with new payment due date
                val studentDocumentRef = studentId?.let { db.collection("Students").document(it) }
                studentDocumentRef?.update("paymentDueDate", calculatedDueDate)
                    ?.addOnSuccessListener {
                        Toast.makeText(
                            applicationContext,
                            "Payment Successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        bottomSheetDialog.dismiss()
                        clearFields()
                        studentData.mobileNo?.let { it1 ->
                            intentToWhatsapp(
                                studentData.name,
                                totalAmt,
                                balanceAmt,
                                calculatedDueDate,
                                it1,
                                remarkTxt
                            )
                        }
                        getPaymentDetailsAndUpdateAdapter(studentId.toString())
                    }?.addOnFailureListener { e ->
                        Toast.makeText(
                            applicationContext,
                            "Error updating student document: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    applicationContext,
                    "Error adding payment: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun calculateDueDate(dateOfJoin: LocalDate): String {
        val selectedItem = bottomSheet.installmentSpinner.selectedItem.toString()
        val formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        val dueDate: String = when (selectedItem) {
            "1 month" -> {
                val nextMonthDateOfJoin = dateOfJoin.plusMonths(1)
                LocalDate.of(
                    nextMonthDateOfJoin.year,
                    nextMonthDateOfJoin.month,
                    dateOfJoin.dayOfMonth
                ).format(formatter)
            }

            "2 months" -> {
                val nextTwoMonthDateOfJoin = dateOfJoin.plusMonths(2)
                LocalDate.of(
                    nextTwoMonthDateOfJoin.year,
                    nextTwoMonthDateOfJoin.month,
                    dateOfJoin.dayOfMonth
                ).format(formatter)
            }

            "3 months" -> {
                val nextQuarterDateOfJoin = dateOfJoin.plusMonths(3)
                LocalDate.of(
                    nextQuarterDateOfJoin.year,
                    nextQuarterDateOfJoin.month,
                    dateOfJoin.dayOfMonth
                ).format(formatter)
            }

            "6 months" -> {
                val nextHalfYearDateOfJoin = dateOfJoin.plusMonths(6)
                LocalDate.of(
                    nextHalfYearDateOfJoin.year,
                    nextHalfYearDateOfJoin.month,
                    dateOfJoin.dayOfMonth
                ).format(formatter)
            }

            "Yearly" -> {
                val nextYearDateOfJoin = dateOfJoin.plusYears(1)
                LocalDate.of(
                    nextYearDateOfJoin.year,
                    nextYearDateOfJoin.month,
                    dateOfJoin.dayOfMonth
                ).format(formatter)
            }

            else -> ""
        }
        return dueDate
    }

    private fun clearFields() {
        bottomSheet.totalAmtEditTxt.text?.clear()
        bottomSheet.balanceAmtEditTxt.text?.clear()
        bottomSheet.remarkEditTxt.text?.clear()
    }

    private fun stringToLocalDate(dateString: String, pattern: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return LocalDate.parse(dateString, formatter)
    }

    private fun intentToWhatsapp(
        name: String?,
        totalAmt: String,
        balanceAmt: String,
        dueDate: String,
        mobileNo: String,
        remarkTxt: String
    ) {
        val whatsappContent = """
*Assalamualaikum,*

We're grateful for your timely tuition fee payment for *$name* at *Idara Rauzat-ul-Ashraf*.
Your payment of *Rs.$totalAmt/-* has been received.
Balance amount: *Rs.$balanceAmt/-*.
Next installment due on *[$dueDate]*.
Remarks: *$remarkTxt*
Your promptness helps us maintain quality education. 
For queries: +918919133812.

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
    }

    private fun getPaymentDetailsAndUpdateAdapter(studentId: String) {
        val db = FirebaseFirestore.getInstance()

        val studentDocumentRef = db.collection("Students").document(studentId)

        val paymentsCollectionRef = studentDocumentRef.collection("Payments")

        paymentsCollectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                val paymentList = mutableListOf<PaymentData>()

                for (document in querySnapshot.documents) {
                    val paymentData = document.toObject(PaymentData::class.java)

                    paymentData?.let {
                        paymentList.add(it)
                    }
                }
                updatePaymentAdapter(paymentList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    applicationContext,
                    "Error Msg = " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updatePaymentAdapter(paymentList: List<PaymentData>) {
        val sortedList = paymentList.sortedByDescending { it.paidDate }

        binding.paymentsRV.setHasFixedSize(true)
        binding.paymentsRV.layoutManager = LinearLayoutManager(applicationContext)
        adapter = PaymentAdapter(sortedList)
        binding.paymentsRV.adapter = adapter

        if (sortedList.isEmpty()) {
            binding.noDataTxtView.visibility = View.VISIBLE
        } else {
            binding.noDataTxtView.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, StudentDetailsActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        fetchAndUpdateData()
    }

    private fun fetchAndUpdateData() {
        val studentId = intent.getStringExtra("studentId")
        studentId?.let {
            // Fetch student data from Fire store
            db.collection("Students")
                .document(studentId)
                .get()
                .addOnSuccessListener { document ->
                    val studentData = document.toObject(Student::class.java)
                    studentData?.let { student ->
                        // Update UI with fetched data
                        binding.stdNameTv.text = student.name
                        binding.fatherNameTv.text = "S/O ${student.fatherName}"
                        binding.mobileNoTv.text = "Mobile No : ${student.mobileNo}"
                        binding.dueDateTv.text = "Due Date : ${student.paymentDueDate}"
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        applicationContext,
                        "Error fetching student data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        getPaymentDetailsAndUpdateAdapter(studentId ?: "")
    }

}

