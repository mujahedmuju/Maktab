package com.example.tuitionproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tuitionproject.R
import com.example.tuitionproject.databinding.ItemPaymentDetailsBinding
import com.example.tuitionproject.model.PaymentData

class PaymentAdapter(private var paymentList: List<PaymentData>) :
    RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_details, parent, false)

        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val paymentItem = paymentList[position]

        holder.binding.apply {
            tvPaidDate.text = ":   "+paymentItem.paidDate
            tvTotalAmountPaid.text = ":   "+paymentItem.totalAmountPaid
            tvBalanceAmount.text = ":   "+paymentItem.balanceAmount
            tvDueDate.text = ":   "+paymentItem.dueDate

            if (paymentItem.remarks.isNotEmpty()) {
                remarksTv.visibility = View.VISIBLE
                remarksTv.text = "Remark : " + paymentItem.remarks
            } else {
                remarksTv.visibility = View.GONE
            }

        }

    }

    override fun getItemCount(): Int {
        return paymentList.size
    }

    class PaymentViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val binding = ItemPaymentDetailsBinding.bind(itemView)
    }
}