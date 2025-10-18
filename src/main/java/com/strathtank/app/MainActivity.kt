package com.strathtank.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var titleEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var uploadCard: MaterialCardView
    private lateinit var uploadButton: MaterialButton
    private lateinit var uploadProgress: CircularProgressIndicator
    private lateinit var filesRecyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    
    private val selectedFiles = mutableListOf<Uri>()
    private lateinit var filesAdapter: FilesAdapter
    
    companion object {
        private const val PICK_FILES_REQUEST = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupClickListeners()
        setupRecyclerView()
        setupBottomNavigation()
        updateUploadButtonState()
    }
    
    private fun initializeViews() {
        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        uploadCard = findViewById(R.id.uploadCard)
        uploadButton = findViewById(R.id.uploadButton)
        uploadProgress = findViewById(R.id.uploadProgress)
        filesRecyclerView = findViewById(R.id.filesRecyclerView)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }
    
    private fun setupClickListeners() {
        uploadCard.setOnClickListener {
            openFilePicker()
        }
        
        uploadButton.setOnClickListener {
            uploadProject()
        }
    }
    
    private fun setupRecyclerView() {
        filesAdapter = FilesAdapter(selectedFiles) { uri ->
            selectedFiles.remove(uri)
            filesAdapter.notifyDataSetChanged()
            updateUploadButtonState()
        }
        filesRecyclerView.layoutManager = LinearLayoutManager(this)
        filesRecyclerView.adapter = filesAdapter
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showHomeFragment()
                    true
                }
                R.id.nav_projects -> {
                    showProjectsFragment()
                    true
                }
                R.id.nav_messages -> {
                    showMessagesFragment()
                    true
                }
                R.id.nav_profile -> {
                    showProfileFragment()
                    true
                }
                else -> false
            }
        }
        
        // Set default selection after a short delay to ensure navigation is ready
        bottomNavigation.post {
            bottomNavigation.selectedItemId = R.id.nav_projects
        }
    }
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Select Files"), PICK_FILES_REQUEST)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PICK_FILES_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.let { intent ->
                val clipData = intent.clipData
                if (clipData != null) {
                    // Multiple files selected
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        if (!selectedFiles.contains(uri)) {
                            selectedFiles.add(uri)
                        }
                    }
                } else {
                    // Single file selected
                    intent.data?.let { uri ->
                        if (!selectedFiles.contains(uri)) {
                            selectedFiles.add(uri)
                        }
                    }
                }
                
                filesAdapter.notifyDataSetChanged()
                updateUploadButtonState()
                updateFilesRecyclerViewVisibility()
            }
        }
    }
    
    private fun updateUploadButtonState() {
        val hasTitle = titleEditText.text?.isNotBlank() == true
        val hasDescription = descriptionEditText.text?.isNotBlank() == true
        val hasFiles = selectedFiles.isNotEmpty()
        
        uploadButton.isEnabled = hasTitle && hasDescription && hasFiles
        uploadButton.alpha = if (uploadButton.isEnabled) 1.0f else 0.6f
    }
    
    private fun updateFilesRecyclerViewVisibility() {
        filesRecyclerView.visibility = if (selectedFiles.isNotEmpty()) View.VISIBLE else View.GONE
    }
    
    private fun uploadProject() {
        val title = titleEditText.text?.toString()?.trim()
        val description = descriptionEditText.text?.toString()?.trim()
        
        if (title.isNullOrBlank() || description.isNullOrBlank()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedFiles.isEmpty()) {
            Toast.makeText(this, "Please select at least one file", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show progress
        uploadProgress.visibility = View.VISIBLE
        uploadButton.isEnabled = false
        uploadButton.text = "Uploading..."
        
        // Simulate upload process
        simulateUpload(title, description)
    }
    
    private fun simulateUpload(title: String, description: String) {
        // This is a simulation - in real app, you would upload to Firebase here
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            uploadProgress.visibility = View.GONE
            uploadButton.isEnabled = true
            uploadButton.text = "Upload Project"
            
            // Show success message
            Toast.makeText(this, "Project uploaded successfully!", Toast.LENGTH_LONG).show()
            
            // Clear form
            clearForm()
        }, 3000) // 3 second simulation
    }
    
    private fun clearForm() {
        titleEditText.text?.clear()
        descriptionEditText.text?.clear()
        selectedFiles.clear()
        filesAdapter.notifyDataSetChanged()
        updateUploadButtonState()
        updateFilesRecyclerViewVisibility()
    }
    
    private fun showHomeFragment() {
        Toast.makeText(this, "Home - Coming Soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showProjectsFragment() {
        // This is the current project upload view - already visible
        Toast.makeText(this, "Projects - Upload your projects here", Toast.LENGTH_SHORT).show()
    }
    
    private fun showMessagesFragment() {
        Toast.makeText(this, "Messages - Coming Soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showProfileFragment() {
        Toast.makeText(this, "Profile - Coming Soon!", Toast.LENGTH_SHORT).show()
    }
}

// Simple adapter for displaying selected files
class FilesAdapter(
    private val files: List<Uri>,
    private val onRemove: (Uri) -> Unit
) : RecyclerView.Adapter<FilesAdapter.FileViewHolder>() {
    
    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.fileName)
        val removeButton: MaterialButton = itemView.findViewById(R.id.removeButton)
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): FileViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.fileName.text = getFileName(file)
        holder.removeButton.setOnClickListener {
            onRemove(file)
        }
    }
    
    override fun getItemCount(): Int = files.size
    
    private fun getFileName(uri: Uri): String {
        return uri.lastPathSegment ?: "Unknown file"
    }
}
