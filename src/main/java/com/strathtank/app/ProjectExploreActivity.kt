package com.strathtank.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class ProjectExploreActivity : AppCompatActivity() {
    
    private lateinit var searchEditText: TextInputEditText
    private lateinit var projectsRecyclerView: RecyclerView
    private lateinit var categoryDropdown: AutoCompleteTextView
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
        categoryDropdown = findViewById(R.id.categoryDropdown)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        projectsCountText = findViewById(R.id.projectsCount)
        fabUpload = findViewById(R.id.fabUpload)
    }
    
    private fun setupClickListeners() {
        // Handle search with sticky navbar and real-time search
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Keep navbar visible when typing - use ADJUST_PAN to prevent navbar from being hidden
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            } else {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
        }
        
        // Add real-time search
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                performSearch()
            }
        })
        
        // Setup FAB click listener
        fabUpload.setOnClickListener {
            navigateToUploadPage()
        }
        
        // Setup category dropdown
        setupCategoryDropdown()
        
        // Add sticky navbar behavior to category dropdown as well
        categoryDropdown.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            } else {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
        }
    }
    
    private fun setupCategoryDropdown() {
        val categories = ProjectCategory.values()
        val categoryNames = mutableListOf("All Categories")
        
        // Add category names to the list
        categories.forEach { category ->
            categoryNames.add(category.name.replace("_", " "))
        }
        
        // Create adapter for dropdown with better styling
        val adapter = CategoryDropdownAdapter(this, categoryNames)
        categoryDropdown.setAdapter(adapter)
        categoryDropdown.threshold = 0 // Show dropdown immediately when clicked
        
        // Set default selection
        categoryDropdown.setText("All Categories", false)
        
        // Ensure dropdown opens when clicked
        categoryDropdown.setOnClickListener {
            categoryDropdown.showDropDown()
        }
        
        // Handle selection with highlighting
        categoryDropdown.setOnItemClickListener { _, _, position, _ ->
            // Update the selected text with better styling
            val selectedCategory = if (position == 0) "All Categories" else categoryNames[position]
            categoryDropdown.setText(selectedCategory, false)
            performSearch() // Use performSearch to handle both category and search query
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
        val selectedCategory = getSelectedCategory()
        
        filteredProjects.clear()
        filteredProjects.addAll(
            allProjects.filter { project ->
                val matchesCategory = selectedCategory == null || project.category == selectedCategory
                val matchesSearch = query.isNullOrBlank() || 
                    project.title.lowercase().contains(query) ||
                    project.description.lowercase().contains(query) ||
                    project.tags.any { it.lowercase().contains(query) }
                
                matchesCategory && matchesSearch
            }
        )
        
        projectsAdapter.notifyDataSetChanged()
        updateProjectsCount()
    }
    
    private fun getSelectedCategory(): ProjectCategory? {
        val selectedText = categoryDropdown.text?.toString()
        if (selectedText == "All Categories") return null
        
        val categories = ProjectCategory.values()
        return categories.find { category ->
            category.name.replace("_", " ") == selectedText
        }
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

// Custom adapter for category dropdown with better styling
class CategoryDropdownAdapter(
    private val context: android.content.Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, items) {
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = items[position]
        textView.setTextColor(0xFF1A1A1A.toInt())
        textView.textSize = 16f
        textView.typeface = android.graphics.Typeface.DEFAULT_BOLD
        textView.setPadding(24, 16, 24, 16)
        
        return view
    }
    
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = items[position]
        textView.setTextColor(0xFF1A1A1A.toInt())
        textView.textSize = 16f
        textView.typeface = android.graphics.Typeface.DEFAULT_BOLD
        textView.setPadding(32, 20, 32, 20)
        
        // Add special highlighting for the default "All Categories" option
        if (position == 0) {
            view.setBackgroundColor(0xFFF0F7FF.toInt()) // Light blue background
        } else {
            view.setBackgroundColor(0xFFFFFFFF.toInt()) // White background
        }
        
        return view
    }
}
