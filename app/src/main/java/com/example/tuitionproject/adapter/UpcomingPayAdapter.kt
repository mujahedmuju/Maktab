package com.example.tuitionproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tuitionproject.R
import com.example.tuitionproject.databinding.ItemUpcomingPayBinding
import com.example.tuitionproject.model.Student

class UpcomingPayAdapter(
    private val studentPaymentList: MutableList<Student> = mutableListOf(),
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<UpcomingPayAdapter.UpcomingPayVH>() {

    interface ItemClickListener {
        fun onItemClick(student: Student)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UpcomingPayAdapter.UpcomingPayVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_upcoming_pay, parent, false)
        return UpcomingPayVH(view)
    }

    override fun getItemCount(): Int {
        return studentPaymentList.size
    }

    fun updateData(updatedList: List<Student>) {
        studentPaymentList.clear()
        studentPaymentList.addAll(updatedList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: UpcomingPayAdapter.UpcomingPayVH, position: Int) {
        val studentPayList = studentPaymentList[position]
        holder.binding.apply {
            stdName.text = studentPayList.name
            fatherName.text = "S/O ${studentPayList.fatherName}"
            phoneNo.text = "Phone No: ${studentPayList.mobileNo}"
            dueDateEditTxt.text = "Due Date: ${studentPayList.paymentDueDate}"
            root.setOnClickListener {
                itemClickListener.onItemClick(studentPayList)
            }
        }
    }

    inner class UpcomingPayVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemUpcomingPayBinding.bind(itemView)
    }
}

