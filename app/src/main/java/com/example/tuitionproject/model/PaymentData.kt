package com.example.tuitionproject.model

data class PaymentData(
    val paidDate: String = "",
    val totalAmountPaid: String = "",
    val balanceAmount: String = "",
    val dueDate: String = "",
    val remarks: String = ""
) {
    // No-argument constructor required by Fire store
    constructor() : this("", "", "", "","")
}
