package com.example.tuitionproject.utils

import com.example.tuitionproject.model.PaymentData
import com.example.tuitionproject.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

object FireStoreHelper {

    private val db = FirebaseFirestore.getInstance()

    fun addDocument(collection: String, student: Student): Task<DocumentReference> {
        return db.collection(collection)
            .add(student)
    }

    fun getDocuments(collection: String): Task<QuerySnapshot> {
        return db.collection(collection)
            .get()
    }

    fun getDocumentById(collection: String, documentId: String): Task<DocumentSnapshot> {
        return db.collection(collection)
            .document(documentId)
            .get()
    }

    fun getDocumentsByField(
        collection: String,
        fieldName: String,
        value: Any
    ): Task<QuerySnapshot> {
        return db.collection(collection)
            .whereEqualTo(fieldName, value)
            .get()
    }

    fun updateDocument(collection: String, documentId: String, data: Map<String, Any>): Task<Void> {
        return db.collection(collection)
            .document(documentId)
            .update(data)
    }

    fun deleteDocument(collection: String, documentId: String): Task<Void> {
        return db.collection(collection)
            .document(documentId)
            .delete()
    }

    fun addPayment(studentId: String, paymentData: PaymentData): Task<DocumentReference> {
        val paymentsCollectionRef = db.collection("Students").document(studentId)
            .collection("Payments")

        return paymentsCollectionRef.add(paymentData)
    }

    fun getDocumentCount(collection: String): Task<Int> {
        val collectionRef = db.collection(collection)

        return collectionRef.get()
            .continueWith { task ->
                if (task.isSuccessful) {
                    // Get the count of documents in the collection
                    task.result?.size() ?: 0
                } else {
                    // Handle the error
                    throw task.exception ?: Exception("Unknown error occurred")
                }
            }
    }
}

