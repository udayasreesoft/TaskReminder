package com.udayasreesoft.adminbusinessanalysis

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.firebase.database.FirebaseDatabase

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val editText : EditText = findViewById(R.id.edit_text_id)
        findViewById<Button>(R.id.add_btn_id).setOnClickListener {
            val value = editText.text.toString()
            if (value.isNotEmpty()) {
                FirebaseDatabase.getInstance()
                    .getReference("Admin")
                    .child("Outlet")
                    .setValue(value)
            }
        }
    }
}
