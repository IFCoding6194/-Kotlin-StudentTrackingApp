package com.example.studenttrackingapp.DataClass

data class Student(
    val name: String = "",
    val classNumber: String = "",
    val section: String = "",
    val schoolName: String = "",
    val gender: String = "",
    val dob: String = "",
    val bloodGroup: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val parentContactNo: String = "",
    val address1: String = "",
    val address2: String? = "",
    val city: String = "",
    val state: String = "",
    val zip: String = "",
    val emergencyNumber: String = "",
    val profileImageBase64: String? = null,
    val location: String? = null
) {
    constructor() : this(
        name = "", classNumber = "", section = "", schoolName = "",
        gender = "", dob = "", bloodGroup = "", fatherName = "",
        motherName = "", parentContactNo = "", address1 = "",
        address2 = "", city = "", state = "", zip = "",
        emergencyNumber = "", profileImageBase64 = null, location = null
    )
}
