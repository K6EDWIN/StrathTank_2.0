package com.strathtank.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class ProjectDetailsActivity : AppCompatActivity() {
    
    private lateinit var project: Project
    private lateinit var titleText: android.widget.TextView
    private lateinit var descriptionText: android.widget.TextView
    private lateinit var authorText: android.widget.TextView
    private lateinit var categoryText: android.widget.TextView
    private lateinit var tagsText: android.widget.TextView
    private lateinit var dateText: android.widget.TextView
    private lateinit var likesText: android.widget.TextView
    private lateinit var viewsText: android.widget.TextView
    private lateinit var linkCard: MaterialCardView
    private lateinit var linkButton: MaterialButton
    private lateinit var likeButton: MaterialButton
    private lateinit var shareButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)
        
        // Get project from intent
        project = intent.getSerializableExtra("project") as Project
        
        initializeViews()
        populateProjectDetails()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        titleText = findViewById(R.id.projectTitle)
        descriptionText = findViewById(R.id.projectDescription)
        authorText = findViewById(R.id.projectAuthor)
        categoryText = findViewById(R.id.projectCategory)
        tagsText = findViewById(R.id.projectTags)
        dateText = findViewById(R.id.projectDate)
        likesText = findViewById(R.id.projectLikes)
        viewsText = findViewById(R.id.projectViews)
        linkCard = findViewById(R.id.linkCard)
        linkButton = findViewById(R.id.linkButton)
        likeButton = findViewById(R.id.likeButton)
        shareButton = findViewById(R.id.shareButton)
    }
    
    private fun populateProjectDetails() {
        titleText.text = project.title
        descriptionText.text = project.description
        authorText.text = "by ${project.authorName}"
        categoryText.text = project.category.name.replace("_", " ")
        
        // Format tags
        val tagsText = project.tags.joinToString(" • ")
        this.tagsText.text = tagsText
        this.tagsText.visibility = if (tagsText.isNotEmpty()) View.VISIBLE else View.GONE
        
        // Format date
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        dateText.text = dateFormat.format(project.createdAt)
        
        // Update stats
        likesText.text = "${project.likes} likes"
        viewsText.text = "${project.views} views"
        
        // Show/hide link section
        if (project.link != null) {
            linkCard.visibility = View.VISIBLE
            linkButton.text = "View Project"
        } else {
            linkCard.visibility = View.GONE
        }
        
        // Update like button state
        updateLikeButton()
    }
    
    private fun setupClickListeners() {
        linkButton.setOnClickListener {
            openProjectLink()
        }
        
        likeButton.setOnClickListener {
            toggleLike()
        }
        
        shareButton.setOnClickListener {
            shareProject()
        }
    }
    
    private fun openProjectLink() {
        if (project.link != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(project.link))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun toggleLike() {
        // Simulate like toggle
        val newLikes = if (likeButton.isSelected) {
            project.likes - 1
        } else {
            project.likes + 1
        }
        
        // Update project object (in real app, this would update the database)
        project = project.copy(likes = newLikes)
        
        // Update UI
        likesText.text = "${project.likes} likes"
        likeButton.isSelected = !likeButton.isSelected
        updateLikeButton()
        
        Toast.makeText(this, if (likeButton.isSelected) "Liked!" else "Unliked!", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateLikeButton() {
        likeButton.isSelected = false // In real app, check if current user liked this project
        likeButton.text = if (likeButton.isSelected) "Liked" else "Like"
    }
    
    private fun shareProject() {
        val shareText = "${project.title}\n\n${project.description}\n\nby ${project.authorName}"
        
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, project.title)
        
        startActivity(Intent.createChooser(shareIntent, "Share Project"))
    }
}
