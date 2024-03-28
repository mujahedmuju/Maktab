package com.example.tuitionproject.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Student(
    val name: String? = "",
    val fatherName: String? = "",
    val mobileNo: String? = "",
    val dob: String? = "",
    val doj: String? = "",
    val timing: String? = "",
    val course: String? = "",
    val address: String? = "",
    var studentId: String? = "",
    var paymentDueDate: String? = "",
//    val studentPhotoUrl: String? = "",
    var isSwiped: Boolean = false

    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(fatherName)
        parcel.writeString(mobileNo)
        parcel.writeString(dob)
        parcel.writeString(doj)
        parcel.writeString(timing)
        parcel.writeString(course)
        parcel.writeString(address)
        parcel.writeString(studentId)
        parcel.writeString(paymentDueDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Student> {
        override fun createFromParcel(parcel: Parcel): Student {
            return Student(parcel)
        }

        override fun newArray(size: Int): Array<Student?> {
            return arrayOfNulls(size)
        }
    }
}
