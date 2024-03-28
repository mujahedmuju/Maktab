package com.example.tuitionproject.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.tuitionproject.R
import com.example.tuitionproject.databinding.ItemStudentDetailsBinding
import com.example.tuitionproject.model.Student
import java.util.Locale

class StudentAdapter(
    val context: Context,
    var studentList: MutableList<Student> = mutableListOf()
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>(), Filterable {

    private var onItemClickListener: ((Student) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_details, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val currentStudent = studentList[position]

        holder.binding.apply {
            stdName.text = currentStudent.name
            fatherName.text = "C/O ${currentStudent.fatherName}"
            phoneNo.text = "Phone No: ${currentStudent.mobileNo}"
            dob.text = "D.O.B: ${currentStudent.dob}"

            holder.itemView.setOnClickListener {
                onItemClickListener?.invoke(currentStudent)
            }

        }
    }

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemStudentDetailsBinding.bind(itemView)
    }

    override fun getItemCount(): Int {
        return studentList.size
    }

    fun updateData(updatedList : List<Student>){
        studentList.clear()
        studentList.addAll(updatedList)
        notifyDataSetChanged()
    }

    // Set click listener externally
    fun setOnItemClickListener(listener: (Student) -> Unit) {
        onItemClickListener = listener
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                val queryString = constraint.toString().lowercase(Locale.getDefault())
                filterResults.values = if (queryString.isEmpty()) {
                    studentList
                } else {
                    studentList.filter { student ->
                        student.name!!.lowercase(Locale.getDefault()).contains(queryString)
                    }
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                studentList = results?.values as MutableList<Student>
                notifyDataSetChanged()
            }
        }
    }
}


// Interface to handle item click
interface OnItemClickListener {
    fun onItemClick(position: Int)
}

interface SwipeActionListener {
    fun onLeftSwipe(student: Student)
}
