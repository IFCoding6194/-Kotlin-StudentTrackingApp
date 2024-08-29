package com.example.studenttrackingapp.Activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studenttrackingapp.DataClass.User
import com.example.studenttrackingapp.R
import com.example.studenttrackingapp.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Set Status Bar Color
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = resources.getColor(R.color.dark_color)

        binding.loginBtn.setOnClickListener {
            signIn()
        }

        binding.signUpLl.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signIn() {
        val email = binding.emailedt.text.toString().trim()
        val password = binding.passwordEdt.text.toString().trim()

        if (TextUtils.isEmpty(email)) {
            binding.emailedt.error = "Enter your email"
            binding.emailedt.requestFocus()
            return
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordEdt.error = "Enter your password"
            binding.passwordEdt.requestFocus()
            return
        }

        if (password.length != 8) {
            binding.passwordEdt.error = "Enter your eight-digit password"
            binding.passwordEdt.requestFocus()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    if (userId != null) {
                        database.child("Users").child(userId).get()
                            .addOnCompleteListener { dataTask ->
                                if (dataTask.isSuccessful) {
                                    val userData = dataTask.result?.getValue(User::class.java)
                                    if (userData != null) {
                                        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                                        val editor = sharedPreferences.edit()
                                        editor.putString("userId", userId)
                                        editor.apply()
                                        val intent = Intent(this, DashboardActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "User data is null",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Failed to retrieve user data: ${dataTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Failed to retrieve user ID", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Sign-in failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
