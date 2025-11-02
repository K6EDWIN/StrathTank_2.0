package com.strathtank.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MessagesActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var contentText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)
        
        initializeViews()
        setupBottomNavigation()
    }
    
    private fun initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        contentText = findViewById(R.id.contentText)
        
        // Set the content text
        contentText.text = "Content of messages page will be placed here"
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = android.content.Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_projects -> {
                    val intent = android.content.Intent(this, ProjectExploreActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_messages -> {
                    // Already in messages
                    true
                }
                R.id.nav_profile -> {
                    val intent = android.content.Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        
        // Set messages as selected
        bottomNavigation.post {
            bottomNavigation.selectedItemId = R.id.nav_messages
        }
    }
}
