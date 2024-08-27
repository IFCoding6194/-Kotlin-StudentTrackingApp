package com.example.studenttrackingapp.Adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.studenttrackingapp.DataClass.Student
import com.example.studenttrackingapp.R
import com.example.studenttrackingapp.Utils.ImageUtils

class StudentDataAdapter(
    private val context: Context,
    private val studentDataList: List<Student>
) : RecyclerView.Adapter<StudentDataAdapter.MyViewHolder>() {

    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_student, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val student = studentDataList[position]
        holder.name_tv.text = student.name
        holder.class_tv.text = student.classNumber
        holder.std_tv.text = student.section
        holder.School_tv.text = student.schoolName

        val profileImageBase64 = student.profileImageBase64
        if (profileImageBase64 != null && profileImageBase64.isNotEmpty()) {
            val bitmap = ImageUtils.decodeBase64(profileImageBase64)
            Glide.with(context)
                .load(bitmap)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.student_img)
        } else {
            holder.student_img.setImageResource(R.drawable.profile)
        }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(position)
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun getItemCount(): Int {
        return studentDataList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val student_img: ImageView = itemView.findViewById(R.id.student_img)
        val name_tv: TextView = itemView.findViewById(R.id.name_tv)
        val class_tv: TextView = itemView.findViewById(R.id.class_tv)
        val std_tv: TextView = itemView.findViewById(R.id.std_tv)
        val School_tv: TextView = itemView.findViewById(R.id.School_tv)
    }

}
