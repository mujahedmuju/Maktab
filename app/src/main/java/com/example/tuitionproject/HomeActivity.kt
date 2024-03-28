package com.example.tuitionproject

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tuitionproject.databinding.ActivityHomeBinding
import com.example.tuitionproject.utils.FireStoreHelper
import com.example.tuitionproject.utils.ProgressDialogHelper
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {
    private val CODE_AUTHENTICATION_VERIFICATION = 241
    private lateinit var binding: ActivityHomeBinding
    private val progressDialog: ProgressDialogHelper by lazy { ProgressDialogHelper(this@HomeActivity) }
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        setupViews()
        checkAndPromptContactDeveloper()
        totalStudentCount()

        if (!sharedPreferences.getBoolean("isLockScreenPasswordSet", false)) {
            setLockScreenPassword()
            sharedPreferences.edit().putBoolean("isLockScreenPasswordSet", true).apply()
        }
    }

    private fun setupViews() {
        binding.stdDetailsLayout.setOnClickListener {
            startActivity(Intent(this@HomeActivity, StudentDetailsActivity::class.java))
        }
        binding.upcomingPayBtn.setOnClickListener {
            startActivity(Intent(this@HomeActivity, UpcomingPayActivity::class.java))
        }
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
        }
    }

    private fun totalStudentCount() {
        progressDialog.showProgressDialog()
        FireStoreHelper.getDocumentCount("Students")
            .addOnSuccessListener { documentCount ->
                binding.totalStudentTv.text = documentCount.toString()
                progressDialog.hideProgressDialog()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    baseContext,
                    "Error getting document count: $e",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setMessage("Are you sure you want to exit?")
            setPositiveButton("Lock & Exit") { _, _ ->
                super.onBackPressed()
                finishAffinity()
            }
            setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            create().show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.edit().clear().apply()
    }

    private fun setLockScreenPassword() {
        val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (km.isKeyguardSecure) {
            val i =
                km.createConfirmDeviceCredentialIntent("Unlock Idara Rauzat Ul Ashraf", "password")
            startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION)
        } else {
            Toast.makeText(this, "No security setup done by user", Toast.LENGTH_SHORT).show()
            AlertDialog.Builder(this).apply {
                setTitle("Set Lock Screen Password")
                setMessage("For your security, you can only use Idara Rauzat Ul Ashraf when the security setup will be done")
                setPositiveButton(Html.fromHtml("<font color='#1890ff'>Setting</font>")) { _, _ ->
                    val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
                    startActivity(intent)
                    finish()
                }
                setNegativeButton(Html.fromHtml("<font color='#000000'>CANCEL</font>")) { _, _ ->
                    finishAffinity()
                }
                setCancelable(false)
                create().apply {
                    window?.setBackgroundDrawableResource(R.color.white)
                    show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION) {
//            Toast.makeText(this, "Success: Verified user's identity", Toast.LENGTH_SHORT).show()
        } else {
            AlertDialog.Builder(this).apply {
                setTitle("Idara Rauzat Ul Ashraf is locked")
                setMessage("For your security, you can only use Idara Rauzat Ul Ashraf when it's unlocked")
                setPositiveButton(Html.fromHtml("<font color='#1890ff'>Unlock</font>")) { _, _ ->
                    val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                    val ii = km.createConfirmDeviceCredentialIntent(
                        "Unlock Idara Rauzat Ul Ashraf",
                        "password"
                    )
                    startActivityForResult(ii, CODE_AUTHENTICATION_VERIFICATION)
                }
                setNegativeButton(Html.fromHtml("<font color='#1890ff'>CANCEL</font>")) { _, _ ->
                    finishAffinity()
                }
                setCancelable(false)
                create().apply {
                    window?.setBackgroundDrawableResource(R.color.white)
                    show()
                }
            }
        }
    }

    private fun checkAndPromptContactDeveloper() {
        val settingsRef = FirebaseFirestore.getInstance().collection("Settings")

        settingsRef.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val contactDeveloperPrompt =
                        document.getBoolean("contactDeveloperPrompt") ?: false
                    if (contactDeveloperPrompt) {
                        val builder = AlertDialog.Builder(this)
                        builder.setMessage("Contact the developer!")
                            .setPositiveButton("OK") { _, _ ->
                                super.onBackPressed()
                                finishAffinity()
                            }
                            .setCancelable(false)
                            .create()
                            .show()
                        break
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    applicationContext,
                    "Error fetching trial settings: $exception",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
