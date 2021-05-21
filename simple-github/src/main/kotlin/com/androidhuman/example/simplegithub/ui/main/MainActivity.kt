package com.androidhuman.example.simplegithub.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.androidhuman.example.simplegithub.databinding.ActivityMainBinding
import com.androidhuman.example.simplegithub.ui.search.SearchActivity
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnActivityMainSearch.setOnClickListener {
            startActivity<SearchActivity>()
        }
    }
}