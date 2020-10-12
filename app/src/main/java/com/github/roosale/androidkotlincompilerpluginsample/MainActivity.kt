package com.github.roosale.androidkotlincompilerpluginsample

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

private val cat = Cat(
    name = "Neferpitou",
    age = 1.3
)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener {
            Toast.makeText(this, cat.toString(), Toast.LENGTH_SHORT).show()
        }
    }

}

//fun main() {
//    println(cat)
//}