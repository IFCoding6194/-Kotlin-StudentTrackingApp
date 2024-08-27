package com.example.studenttrackingapp.Activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studenttrackingapp.Activity.SignInActivity
import com.example.studenttrackingapp.DataClass.User
import com.example.studenttrackingapp.R
import com.example.studenttrackingapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Set Status Bar Color
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = resources.getColor(R.color.dark_color)

        binding.signInLl.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.signupBtn.setOnClickListener {
            signUp()
        }
    }

    private fun signUp() {
        val name = binding.usernameEdt.text.toString().trim()
        val phoneNo = binding.phoneNoEdt.text.toString().trim()
        val emailId = binding.emailEdt.text.toString().trim()
        val password = binding.passwordEdt.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEdt.text.toString().trim()

        if (TextUtils.isEmpty(name)) {
            binding.usernameEdt.error = "Enter a User Name"
            binding.usernameEdt.requestFocus()
            return
        }

        if (TextUtils.isEmpty(phoneNo)) {
            binding.phoneNoEdt.error = "Enter a Phone Number"
            binding.phoneNoEdt.requestFocus()
            return
        }
        if (!Patterns.PHONE.matcher(phoneNo).matches()) {
            binding.phoneNoEdt.error = "Invalid phone Number"
            binding.phoneNoEdt.requestFocus()
            return
        }

        if (TextUtils.isEmpty(emailId)) {
            binding.emailEdt.error = "Enter an Email"
            binding.emailEdt.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailId).matches()) {
            binding.emailEdt.error = "Enter a valid Email"
            binding.emailEdt.requestFocus()
            return
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordEdt.error = "Enter a Password"
            binding.passwordEdt.requestFocus()
            return
        }

        if (password.length != 8) {
            binding.passwordEdt.error = "Password must be 8 characters"
            binding.passwordEdt.requestFocus()
            return
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.confirmPasswordEdt.error = "Enter a Confirm Password"
            binding.confirmPasswordEdt.requestFocus()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        auth.createUserWithEmailAndPassword(emailId, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid
                    val userObject = User(name, phoneNo, emailId)

                    userId?.let {
                        database.getReference("Users").child(it).setValue(userObject)
                            .addOnCompleteListener { saveTask ->
                                if (saveTask.isSuccessful) {
                                    Toast.makeText(
                                        this@SignUpActivity,
                                        "Sign up successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@SignUpActivity,
                                        "Failed to save user information: ${saveTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Sign up failed: ${task.exception?.message}",
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
