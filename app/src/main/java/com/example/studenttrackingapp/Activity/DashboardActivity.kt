package com.example.studenttrackingapp.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.studenttrackingapp.R
import com.example.studenttrackingapp.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var backPressedTime: Long = 0
    private val backPressedInterval: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Set Status Bar Color
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = resources.getColor(R.color.dark_color)

        displayUserName()

        binding.addStudentLl.setOnClickListener {
            val intent = Intent(this, AddStudentActivity::class.java)
            startActivity(intent)
        }

        binding.viewStudentLl.setOnClickListener {
            val intent = Intent(this, ViewStudentDataActivity::class.java)
            startActivity(intent)
        }
        binding.mapViewLl.setOnClickListener {
            val intent = Intent(this, MapViewActivity::class.java)
            startActivity(intent)
        }

        binding.menuImg.setOnClickListener {
            showLogoutDialog()
        }


    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")

        builder.setPositiveButton("OK") { dialog, _ ->
            val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("userId")
            editor.apply()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            finish()

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun displayUserName() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val usersRef = database.child("Users").child(userId)

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userName = snapshot.child("userName").getValue(String::class.java)
                        binding.userNameTv.text = userName ?: "User Name"
                    } else {
                        binding.userNameTv.text = "User Name"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@DashboardActivity,
                        "Failed to retrieve user name: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            binding.userNameTv.text = "User Name"
        }
    }


    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < backPressedInterval) {
            super.onBackPressed()
            finish()
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
            backPressedTime = currentTime
        }
    }
}
