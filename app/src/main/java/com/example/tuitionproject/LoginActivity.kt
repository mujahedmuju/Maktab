package com.example.tuitionproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.tuitionproject.databinding.ActivityLoginBinding
import com.example.tuitionproject.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        firebaseDatabase = FirebaseDatabase.getInstance()
//        databaseReference = firebaseDatabase.reference.child("users")

//        binding.loginBtn.setOnClickListener {
//            val loginUsername = binding.userIdEdtTxt.text.toString().trim()
//            val loginPassword = binding.passwordEdtTxt.text.toString().trim()
//
//            if (loginUsername.isNotEmpty() && loginPassword.isNotEmpty()) {
//                loginUser(loginUsername, loginPassword)
//            } else {
//                Toast.makeText(
//                    this@LoginActivity,
//                    "All fields are mandatory!",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//
//            binding.registerBtn.setOnClickListener {
//                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
//                finish()
//            }
//        }

        firebaseAuth = FirebaseAuth.getInstance()

        binding.registerBtn.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            finish()
        }

        binding.loginBtn.setOnClickListener {
            val registerEmail = binding.emailEdtTxt.text.toString().trim()
            val registerPassword = binding.passwordEdtTxt.text.toString().trim()

            if (registerEmail.isNotEmpty() && registerPassword.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(registerEmail, registerPassword)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Login Successfully",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                it.exception?.message ?: toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

            } else {
                Toast.makeText(
                    this@LoginActivity, "Empty fields are not allowed!!", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loginUser(username: String, password: String) {
        databaseReference.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val userData = userSnapshot.getValue(UserData::class.java)
                            if (userData != null && userData.password == password) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login Successfully",
                                    Toast.LENGTH_LONG
                                ).show()
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                finish()
                                return
                            }
                        }
                    }
                    Toast.makeText(
                        this@LoginActivity,
                        "Login Failed",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Database Error: ${databaseError.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            })
    }
}