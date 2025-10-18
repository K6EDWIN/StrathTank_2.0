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
    private lateinit var linkEditText: TextInputEditText
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
        linkEditText = findViewById(R.id.linkEditText)
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
        
        // Add text change listeners to update upload button state
        titleEditText.setOnFocusChangeListener { _, _ -> updateUploadButtonState() }
        descriptionEditText.setOnFocusChangeListener { _, _ -> updateUploadButtonState() }
        linkEditText.setOnFocusChangeListener { _, _ -> updateUploadButtonState() }
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
                    updateProfileIcon(false)
                    true
                }
                R.id.nav_projects -> {
                    showProjectsFragment()
                    updateProfileIcon(false)
                    true
                }
                R.id.nav_messages -> {
                    showMessagesFragment()
                    updateProfileIcon(false)
                    true
                }
                R.id.nav_profile -> {
                    showProfileFragment()
                    updateProfileIcon(true)
                    true
                }
                else -> false
            }
        }
        
        // Set default selection after a short delay to ensure navigation is ready
        bottomNavigation.post {
            bottomNavigation.selectedItemId = R.id.nav_projects
            updateProfileIcon(false)
        }
    }
    
    private fun updateProfileIcon(isActive: Boolean) {
        val profileItem = bottomNavigation.menu.findItem(R.id.nav_profile)
        profileItem?.setIcon(if (isActive) R.drawable.ic_profile_filled else R.drawable.ic_profile)
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
        val hasLink = linkEditText.text?.isNotBlank() == true
        
        uploadButton.isEnabled = hasTitle && hasDescription && (hasFiles || hasLink)
        uploadButton.alpha = if (uploadButton.isEnabled) 1.0f else 0.6f
    }
    
    private fun updateFilesRecyclerViewVisibility() {
        filesRecyclerView.visibility = if (selectedFiles.isNotEmpty()) View.VISIBLE else View.GONE
    }
    
    private fun uploadProject() {
        val title = titleEditText.text?.toString()?.trim()
        val description = descriptionEditText.text?.toString()?.trim()
        val link = linkEditText.text?.toString()?.trim()
        
        if (title.isNullOrBlank() || description.isNullOrBlank()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedFiles.isEmpty() && link.isNullOrBlank()) {
            Toast.makeText(this, "Please provide either files or a project link", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show progress
        uploadProgress.visibility = View.VISIBLE
        uploadButton.isEnabled = false
        uploadButton.text = "Uploading..."
        
        // Simulate upload process
        simulateUpload(title, description, link)
    }
    
    private fun simulateUpload(title: String, description: String, link: String?) {
        // This is a simulation - in real app, you would upload to Firebase here
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            uploadProgress.visibility = View.GONE
            uploadButton.isEnabled = true
            uploadButton.text = "Upload Project"
            
            // Show success message
            val message = if (link != null) {
                "Project with link uploaded successfully!"
            } else {
                "Project uploaded successfully!"
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            
            // Clear form
            clearForm()
        }, 3000) // 3 second simulation
    }
    
    private fun clearForm() {
        titleEditText.text?.clear()
        descriptionEditText.text?.clear()
        linkEditText.text?.clear()
        selectedFiles.clear()
        filesAdapter.notifyDataSetChanged()
        updateUploadButtonState()
        updateFilesRecyclerViewVisibility()
    }
    
    private fun showHomeFragment() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
    
    private fun showProjectsFragment() {
        // Navigate to Project Explore Activity
        val intent = Intent(this, ProjectExploreActivity::class.java)
        startActivity(intent)
    }
    
    private fun showMessagesFragment() {
        val intent = Intent(this, MessagesActivity::class.java)
        startActivity(intent)
    }
    
    private fun showProfileFragment() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
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
