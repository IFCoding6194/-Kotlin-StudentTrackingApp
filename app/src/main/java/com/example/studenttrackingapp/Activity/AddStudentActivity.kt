package com.example.studenttrackingapp.Activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.icu.util.Calendar
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.studenttrackingapp.DataClass.Student
import com.example.studenttrackingapp.R
import com.example.studenttrackingapp.databinding.ActivityAddStudentBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth

class AddStudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStudentBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var lastClickTime = 0L
    private var popupMenuShowing = false

    companion object {
        private const val LOCATION_REQUEST_CODE = 100
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_PICK_IMAGE = 2
    }

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            showImageSourceDialog()
        } else {
            if (!permissions.values.any { it }) {
                Toast.makeText(
                    this,
                    "Permissions required to access camera/gallery",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set Status Bar Color
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = resources.getColor(R.color.statusbar_color)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("students")

        binding.backButtonBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            saveStudentData()
        }

        binding.fullMapImg.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivityForResult(intent, LOCATION_REQUEST_CODE)
        }

        binding.selectLocationButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivityForResult(intent, LOCATION_REQUEST_CODE)
        }

        binding.locationText.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivityForResult(intent, LOCATION_REQUEST_CODE)
        }

        binding.studentImg.isEnabled = true
        binding.studentImg.isClickable = true

        binding.studentImg.setOnClickListener {
            if (System.currentTimeMillis() - lastClickTime < 1000) {
                return@setOnClickListener
            }
            lastClickTime = System.currentTimeMillis()

            requestPermissions()
        }

        binding.studentImg.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                requestPermissions()
                v.performClick()
            }
            true
        }

        binding.dateDobEdt.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.dateDobEdt.setText(date)
            }, year, month, day
        )

        datePickerDialog.show()
    }

    private fun requestPermissions() {
        if (arePermissionsGranted()) {
            showImageSourceDialog()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun arePermissionsGranted(): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun showImageSourceDialog() {
        if (popupMenuShowing) return

        val popupMenu = PopupMenu(this, binding.studentImg)
        popupMenu.inflate(R.menu.image_source_menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.take_photo -> {
                    dispatchTakePictureIntent()
                    true
                }

                R.id.choose_from_gallery -> {
                    dispatchPickPictureIntent()
                    true
                }

                else -> false
            }
        }

        popupMenu.setOnDismissListener {
            popupMenuShowing = false
        }

        popupMenuShowing = true
        popupMenu.show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun dispatchPickPictureIntent() {
        Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).also { pickIntent ->
            pickIntent.type = "image/*"
            startActivityForResult(pickIntent, REQUEST_PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    Glide.with(this)
                        .load(imageBitmap)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.studentImg)
                }

                REQUEST_PICK_IMAGE -> {
                    val selectedImage = data?.data
                    Glide.with(this)
                        .load(selectedImage)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.studentImg)
                }
            }
        }

        if (requestCode == LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val address = data?.getStringExtra("address")
            binding.locationText.setText(address)
        }
    }

    private fun getSelectedGender(): String? {
        return when (binding.genderGroupEdt.checkedRadioButtonId) {
            R.id.radio_Male -> binding.radioMale.text.toString()
            R.id.radio_Female -> binding.radioFemale.text.toString()
            else -> null
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    private fun isDefaultImage(): Boolean {
        val defaultDrawable = resources.getDrawable(R.drawable.profile, null)
        val defaultBitmap = (defaultDrawable as? BitmapDrawable)?.bitmap

        val currentDrawable = binding.studentImg.drawable
        val currentBitmap = (currentDrawable as? BitmapDrawable)?.bitmap

        return defaultBitmap != null && currentBitmap != null && defaultBitmap.sameAs(currentBitmap)
    }

    private fun saveStudentData() {
        val name = binding.nameEdt.text.toString().trim()
        val classNumber = binding.classEdt.text.toString().trim()
        val section = binding.sectionEdt.text.toString().trim()
        val schoolName = binding.schoolEdt.text.toString().trim()
        val gender = getSelectedGender()
        val dob = binding.dateDobEdt.text.toString().trim()
        val bloodGroup = binding.bloodGroupEdt.text.toString().trim()
        val fatherName = binding.fatherNameEdt.text.toString().trim()
        val motherName = binding.motherNameEdt.text.toString().trim()
        val parentContactNo = binding.parentContactEdt.text.toString().trim()
        val address1 = binding.address1Edt.text.toString().trim()
        val address2 = binding.address2Edt.text.toString().trim()
        val city = binding.cityEdt.text.toString().trim()
        val state = binding.stateEdt.text.toString().trim()
        val zip = binding.zipEdt.text.toString().trim()
        val emergencyNumber = binding.emergencyContactEdt.text.toString().trim()
        val studentImg = (binding.studentImg.drawable as? BitmapDrawable)?.bitmap
        val location = binding.locationText.text.toString().trim()

        if (name.isEmpty()) {
            binding.nameEdt.error = "Enter a name"
            binding.nameEdt.requestFocus()
            return
        }

        if (classNumber.isEmpty()) {
            binding.classEdt.error = "Enter a class number"
            binding.classEdt.requestFocus()
            return
        }

        if (section.isEmpty()) {
            binding.sectionEdt.error = "Enter a section"
            binding.sectionEdt.requestFocus()
            return
        }

        if (schoolName.isEmpty()) {
            binding.schoolEdt.error = "Enter a school name"
            binding.schoolEdt.requestFocus()
            return
        }

        if (gender.isNullOrEmpty()) {
            Toast.makeText(this, "Select a gender", Toast.LENGTH_SHORT).show()
            return
        }

        if (dob.isEmpty()) {
            binding.dateDobEdt.error = "Enter date of birth"
            binding.dateDobEdt.requestFocus()
            return
        }

        if (bloodGroup.isEmpty()) {
            binding.bloodGroupEdt.error = "Enter a blood group"
            binding.bloodGroupEdt.requestFocus()
            return
        }

        if (fatherName.isEmpty()) {
            binding.fatherNameEdt.error = "Enter father's name"
            binding.fatherNameEdt.requestFocus()
            return
        }

        if (motherName.isEmpty()) {
            binding.motherNameEdt.error = "Enter mother's name"
            binding.motherNameEdt.requestFocus()
            return
        }

        if (parentContactNo.isEmpty()) {
            binding.parentContactEdt.error = "Enter parent contact number"
            binding.parentContactEdt.requestFocus()
            return
        }

        if (!Patterns.PHONE.matcher(parentContactNo).matches()) {
            binding.parentContactEdt.error = "Invalid phone number"
            binding.parentContactEdt.requestFocus()
            return
        }

        if (address1.isEmpty()) {
            binding.address1Edt.error = "Enter address"
            binding.address1Edt.requestFocus()
            return
        }

        if (city.isEmpty()) {
            binding.cityEdt.error = "Enter a city"
            binding.cityEdt.requestFocus()
            return
        }

        if (state.isEmpty()) {
            binding.stateEdt.error = "Enter a state"
            binding.stateEdt.requestFocus()
            return
        }

        if (zip.isEmpty()) {
            binding.zipEdt.error = "Enter a zip code"
            binding.zipEdt.requestFocus()
            return
        }

        if (emergencyNumber.isEmpty()) {
            binding.emergencyContactEdt.error = "Enter an emergency contact number"
            binding.emergencyContactEdt.requestFocus()
            return
        }

        if (!Patterns.PHONE.matcher(emergencyNumber).matches()) {
            binding.emergencyContactEdt.error = "Invalid emergency phone number"
            binding.emergencyContactEdt.requestFocus()
            return
        }

        if (isDefaultImage()) {
            Toast.makeText(this, "Please upload student image", Toast.LENGTH_SHORT).show()
            return
        }

        if (location.isEmpty() || location == "Select Your Location") {
            Toast.makeText(this, "Please add location", Toast.LENGTH_SHORT).show()
            return
        }

        val student = Student(
            name,
            classNumber,
            section,
            schoolName,
            gender,
            dob,
            bloodGroup,
            fatherName,
            motherName,
            parentContactNo,
            address1,
            address2,
            city,
            state,
            zip,
            emergencyNumber,
            convertBitmapToBase64(studentImg),
            location
        )

        binding.progressBar.visibility = View.VISIBLE
        val userId = auth.currentUser?.uid ?: run {
            binding.progressBar.visibility = View.GONE
            return
        }
        val studentId = database.child("Users").child(userId).child("students").push().key ?: run {
            binding.progressBar.visibility = View.GONE
            return
        }

        database.child("Users").child(userId).child("students").child(studentId).setValue(student)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to add student", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
