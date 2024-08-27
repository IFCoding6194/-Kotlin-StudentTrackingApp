package com.example.studenttrackingapp.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studenttrackingapp.Adapter.StudentDataAdapter
import com.example.studenttrackingapp.DataClass.Student
import com.example.studenttrackingapp.R
import com.example.studenttrackingapp.databinding.ActivityViewStudentDataBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ViewStudentDataActivity : AppCompatActivity(), StudentDataAdapter.OnItemClickListener {
    private lateinit var binding: ActivityViewStudentDataBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val studentList: ArrayList<Student> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewStudentDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set Status Bar Color
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = resources.getColor(R.color.statusbar_color)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("students")

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        getStudentData()
    }

    private fun getStudentData() {
        binding.progressBar.visibility = View.VISIBLE
        val userId = auth.currentUser?.uid ?: return
        database.child("Users").child(userId).child("students")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    studentList.clear()

                    if (snapshot.exists()) {
                        for (studentSnapshot in snapshot.children) {
                            val student = studentSnapshot.getValue(Student::class.java)
                            if (student != null) {
                                studentList.add(student)
                            }
                        }
                        if (studentList.isEmpty()) {
                            println("No student data available")
                            binding.noDataImg.visibility = View.VISIBLE
                        } else {
                            setupRecyclerView()
                        }
                    } else {
                        println("No student data available")
                        binding.noDataImg.visibility = View.VISIBLE
                    }

                    binding.progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ViewStudentDataActivity,
                        "Failed to retrieve data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressBar.visibility = View.GONE
                }
            })
    }


    private fun setupRecyclerView() {
        val adapter = StudentDataAdapter(this, studentList)
        binding.studentDataRcv.adapter = adapter
        binding.studentDataRcv.layoutManager = LinearLayoutManager(this)

        adapter.setOnItemClickListener(this)
    }

    override fun onItemClick(position: Int) {
        val student = studentList[position]
        val intent =
            Intent(this@ViewStudentDataActivity, StudentDetailsActivity::class.java).apply {
                putExtra("studentImg", student.profileImageBase64)
                putExtra("name", student.name)
                putExtra("section", student.section)
                putExtra("classNumber", student.classNumber)
                putExtra("schoolName", student.schoolName)
                putExtra("gender", student.gender)
                putExtra("dob", student.dob)
                putExtra("bloodGroup", student.bloodGroup)
                putExtra("fatherName", student.fatherName)
                putExtra("motherName", student.motherName)
                putExtra("parentContactNo", student.parentContactNo)
                putExtra("address1", student.address1)
                putExtra("address2", student.address2)
                putExtra("city", student.city)
                putExtra("state", student.state)
                putExtra("zip", student.zip)
                putExtra("emergencyNumber", student.emergencyNumber)
                putExtra("location", student.location)
            }
        startActivity(intent)
    }
}
