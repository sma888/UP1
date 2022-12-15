package com.example.filleword

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner

class MainActivity : AppCompatActivity() {
    //val spinner:Spinner = findViewById(R.id.spinner)
    //val choose = resources.getStringArray(R.array.themesWords)
    //var selected=0
    override fun onCreate(savedInstanceState: Bundle?) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        setTheme(R.style.myStyle)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//
//        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                itemSelected: View, selectedItemPosition: Int, selectedId: Long
//            ) {
//                if (choose[selectedItemPosition]=="Программист")
//                    selected=0
//                else if (choose[selectedItemPosition]=="Машина")
//                    selected=1
//                else if (choose[selectedItemPosition]=="Компьютер")
//                    selected=2
//            }
//            override fun onNothingSelected(parent: AdapterView<*>?) {}
//        }

    }

    fun gameButton(view: View) {


        val intent:Intent = Intent(this@MainActivity, GameActivity::class.java)
        startActivity(intent)

    }

    fun Info(view: View) {
        val intent:Intent = Intent(this@MainActivity, InfoActivity::class.java)
        startActivity(intent)
    }
}