package com.example.tuitionproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.tuitionproject.databinding.ActivityRegisterBinding
import com.example.tuitionproject.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }
        binding.registerBtn.setOnClickListener {
            val registerEmail = binding.userIdEdtTxt.text.toString().trim()
            val registerPassword = binding.passwordEdtTxt.text.toString().trim()
            val registerConfirmPassword = binding.confirmPasswordEdtTxt.text.toString().trim()

            if (registerEmail.isNotEmpty() && registerPassword.isNotEmpty() && registerConfirmPassword.isNotEmpty()) {
                if (registerPassword == registerConfirmPassword) {
                    firebaseAuth.createUserWithEmailAndPassword(
                        registerEmail, registerConfirmPassword
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Register Successfully",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                it.exception?.message ?: toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@RegisterActivity, "Password is not matching...", Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@RegisterActivity, "Empty fields are not allowed!!", Toast.LENGTH_LONG
                ).show()
            }
        }

//        firebaseDatabase = FirebaseDatabase.getInstance()
//        databaseReference = firebaseDatabase.reference.child("users")
//        binding.registerBtn.setOnClickListener {
//            val registerUsername = binding.userIdEdtTxt.text.toString().trim()
//            val registerPassword = binding.passwordEdtTxt.text.toString().trim()
//
//            if (registerUsername.isNotEmpty() && registerPassword.isNotEmpty()) {
//                registerUser(registerUsername, registerPassword)
//            } else {
//                Toast.makeText(
//                    this@RegisterActivity,
//                    "All fields are mandatory!",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//
//            binding.loginBtn.setOnClickListener {
//                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
//                finish()
//            }
//        }
    }

    private fun registerUser(username: String, password: String) {
        databaseReference.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        val id = databaseReference.push().key
                        val userData = UserData(id, username, password)
                        databaseReference.child(id!!).setValue(userData)
                        Toast.makeText(
                            this@RegisterActivity, "Register Successfully", Toast.LENGTH_LONG
                        ).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@RegisterActivity, "User already exists!", Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Database Error: ${databaseError.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}