package com.strathtank.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class ProjectsAdapter(
    private val projects: List<Project>,
    private val onProjectClick: (Project) -> Unit
) : RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {
    
    class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.projectCard)
        val titleText: TextView = itemView.findViewById(R.id.projectTitle)
        val descriptionText: TextView = itemView.findViewById(R.id.projectDescription)
        val authorText: TextView = itemView.findViewById(R.id.projectAuthor)
        val categoryText: TextView = itemView.findViewById(R.id.projectCategory)
        val likesText: TextView = itemView.findViewById(R.id.projectLikes)
        val viewsText: TextView = itemView.findViewById(R.id.projectViews)
        val linkIcon: ImageView = itemView.findViewById(R.id.linkIcon)
        val tagsText: TextView = itemView.findViewById(R.id.projectTags)
        val dateText: TextView = itemView.findViewById(R.id.projectDate)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        
        holder.titleText.text = project.title
        holder.descriptionText.text = project.description
        holder.authorText.text = "by ${project.authorName}"
        holder.categoryText.text = project.category.name.replace("_", " ")
        holder.likesText.text = "${project.likes} likes"
        holder.viewsText.text = "${project.views} views"
        
        // Show/hide link icon based on whether project has a link
        holder.linkIcon.visibility = if (project.link != null) View.VISIBLE else View.GONE
        
        // Format tags
        val tagsText = project.tags.joinToString(" • ")
        holder.tagsText.text = tagsText
        holder.tagsText.visibility = if (tagsText.isNotEmpty()) View.VISIBLE else View.GONE
        
        // Format date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.dateText.text = dateFormat.format(project.createdAt)
        
        // Set click listener
        holder.cardView.setOnClickListener {
            onProjectClick(project)
        }
    }
    
    override fun getItemCount(): Int = projects.size
}
