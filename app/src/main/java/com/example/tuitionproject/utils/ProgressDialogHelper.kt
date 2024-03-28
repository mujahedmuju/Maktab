package com.example.tuitionproject.utils

import androidx.appcompat.app.AlertDialog
import android.content.Context
import com.example.tuitionproject.R

class ProgressDialogHelper(private val context: Context) {

    private var dialog: AlertDialog? = null

    fun showProgressDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        dialog = builder.create()
        dialog?.show()
    }

    fun hideProgressDialog() {
        dialog?.dismiss()
    }
}