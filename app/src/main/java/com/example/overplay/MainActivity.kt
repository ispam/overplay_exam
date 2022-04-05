package com.example.overplay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.overplay.databinding.ActivityMainBinding
import com.example.overplay.utils.viewBinding

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}