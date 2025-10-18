package com.strathtank.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class ProjectExploreActivity : AppCompatActivity() {
    
    private lateinit var searchEditText: TextInputEditText
    private lateinit var projectsRecyclerView: RecyclerView
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var projectsCountText: android.widget.TextView
    private lateinit var fabUpload: com.google.android.material.floatingactionbutton.FloatingActionButton
    
    private lateinit var projectsAdapter: ProjectsAdapter
    private val allProjects = mutableListOf<Project>()
    private val filteredProjects = mutableListOf<Project>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_explore)
        
        initializeViews()
        setupClickListeners()
        setupRecyclerView()
        setupBottomNavigation()
        setupTooltip()
        loadSampleProjects()
    }
    
    private fun initializeViews() {
        searchEditText = findViewById(R.id.searchEditText)
        projectsRecyclerView = findViewById(R.id.projectsRecyclerView)
        filterChipGroup = findViewById(R.id.filterChipGroup)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        projectsCountText = findViewById(R.id.projectsCount)
        fabUpload = findViewById(R.id.fabUpload)
    }
    
    private fun setupClickListeners() {
        searchEditText.setOnFocusChangeListener { _, _ -> 
            performSearch()
        }
        
        // Setup FAB click listener
        fabUpload.setOnClickListener {
            navigateToUploadPage()
        }
        
        // Setup filter chips
        setupFilterChips()
    }
    
    private fun setupFilterChips() {
        val categories = ProjectCategory.values()
        
        // Add "All" chip
        val allChip = Chip(this)
        allChip.text = "All"
        allChip.isChecked = true
        allChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                clearOtherChips()
                filterProjects(null)
            }
        }
        filterChipGroup.addView(allChip)
        
        // Add category chips
        categories.forEach { category ->
            val chip = Chip(this)
            chip.text = category.name.replace("_", " ")
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    clearOtherChips()
                    chip.isChecked = true
                    filterProjects(category)
                }
            }
            filterChipGroup.addView(chip)
        }
    }
    
    private fun clearOtherChips() {
        for (i in 0 until filterChipGroup.childCount) {
            val chip = filterChipGroup.getChildAt(i) as Chip
            if (chip != filterChipGroup.getChildAt(0)) { // Don't clear "All" chip
                chip.isChecked = false
            }
        }
    }
    
    private fun setupRecyclerView() {
        projectsAdapter = ProjectsAdapter(filteredProjects) { project ->
            openProjectDetails(project)
        }
        projectsRecyclerView.layoutManager = LinearLayoutManager(this)
        projectsRecyclerView.adapter = projectsAdapter
    }
    
    private fun setupTooltip() {
        // Set tooltip for FAB
        fabUpload.tooltipText = "Upload Project"
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_projects -> {
                    // Already in projects
                    true
                }
                R.id.nav_messages -> {
                    val intent = Intent(this, MessagesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        
        // Set projects as selected
        bottomNavigation.post {
            bottomNavigation.selectedItemId = R.id.nav_projects
        }
    }
    
    private fun loadSampleProjects() {
        // Sample projects for demonstration
        allProjects.addAll(listOf(
            Project(
                title = "E-Commerce Mobile App",
                description = "A comprehensive mobile application for online shopping with features like cart management, payment integration, and user reviews.",
                link = "https://github.com/user/ecommerce-app",
                authorName = "John Doe",
                authorId = "user1",
                category = ProjectCategory.MOBILE_DEVELOPMENT,
                tags = listOf("React Native", "Firebase", "E-commerce"),
                status = ProjectStatus.APPROVED,
                likes = 45,
                views = 234
            ),
            Project(
                title = "Machine Learning Model for Image Recognition",
                description = "Deep learning model trained on custom dataset for accurate image classification with 95% accuracy.",
                link = "https://github.com/user/ml-image-recognition",
                authorName = "Jane Smith",
                authorId = "user2",
                category = ProjectCategory.MACHINE_LEARNING,
                tags = listOf("Python", "TensorFlow", "Computer Vision"),
                status = ProjectStatus.APPROVED,
                likes = 78,
                views = 456
            ),
            Project(
                title = "Responsive Web Portfolio",
                description = "Modern, responsive portfolio website built with React and styled with CSS Grid and Flexbox.",
                link = "https://portfolio.example.com",
                authorName = "Mike Johnson",
                authorId = "user3",
                category = ProjectCategory.WEB_DEVELOPMENT,
                tags = listOf("React", "CSS3", "Responsive Design"),
                status = ProjectStatus.APPROVED,
                likes = 32,
                views = 189
            ),
            Project(
                title = "Data Analysis Dashboard",
                description = "Interactive dashboard for analyzing sales data with real-time charts and filtering capabilities.",
                link = "https://github.com/user/data-dashboard",
                authorName = "Sarah Wilson",
                authorId = "user4",
                category = ProjectCategory.DATA_SCIENCE,
                tags = listOf("Python", "Pandas", "Plotly", "Dash"),
                status = ProjectStatus.APPROVED,
                likes = 56,
                views = 312
            ),
            Project(
                title = "2D Platformer Game",
                description = "Retro-style 2D platformer game with multiple levels, power-ups, and boss battles.",
                authorName = "Alex Brown",
                authorId = "user5",
                category = ProjectCategory.GAME_DEVELOPMENT,
                tags = listOf("Unity", "C#", "2D Game"),
                status = ProjectStatus.APPROVED,
                likes = 89,
                views = 567
            )
        ))
        
        filteredProjects.addAll(allProjects)
        projectsAdapter.notifyDataSetChanged()
        updateProjectsCount()
    }
    
    private fun updateProjectsCount() {
        val count = filteredProjects.size
        projectsCountText.text = "$count project${if (count != 1) "s" else ""} found"
    }
    
    private fun performSearch() {
        val query = searchEditText.text?.toString()?.trim()?.lowercase()
        
        if (query.isNullOrBlank()) {
            filteredProjects.clear()
            filteredProjects.addAll(allProjects)
        } else {
            filteredProjects.clear()
            filteredProjects.addAll(
                allProjects.filter { project ->
                    project.title.lowercase().contains(query) ||
                    project.description.lowercase().contains(query) ||
                    project.tags.any { it.lowercase().contains(query) }
                }
            )
        }
        
        projectsAdapter.notifyDataSetChanged()
        updateProjectsCount()
    }
    
    private fun filterProjects(category: ProjectCategory?) {
        val query = searchEditText.text?.toString()?.trim()?.lowercase()
        
        filteredProjects.clear()
        filteredProjects.addAll(
            allProjects.filter { project ->
                val matchesCategory = category == null || project.category == category
                val matchesSearch = query.isNullOrBlank() || 
                    project.title.lowercase().contains(query) ||
                    project.description.lowercase().contains(query) ||
                    project.tags.any { it.lowercase().contains(query) }
                
                matchesCategory && matchesSearch
            }
        )
        
        projectsAdapter.notifyDataSetChanged()
    }
    
    private fun openProjectDetails(project: Project) {
        val intent = Intent(this, ProjectDetailsActivity::class.java)
        intent.putExtra("project", project)
        startActivity(intent)
    }
    
    private fun navigateToUploadPage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        // Don't call finish() - let user navigate back if needed
    }
}
