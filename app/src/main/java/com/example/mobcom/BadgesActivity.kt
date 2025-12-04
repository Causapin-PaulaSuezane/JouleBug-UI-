package com.example.mobcom

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mobcom.databinding.ActivityBadgesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BadgesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBadgesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBadgesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupUI()
        loadBadges()
    }

    private fun setupUI() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Setup RecyclerView with Grid Layout
        binding.rvBadges.layoutManager = GridLayoutManager(this, 2)
    }

    private fun loadBadges() {
        val currentUserId = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = android.view.View.VISIBLE

        firestore.collection("users")
            .document(currentUserId)
            .collection("badges")
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = android.view.View.GONE

                if (documents.isEmpty) {
                    binding.tvEmptyState.visibility = android.view.View.VISIBLE
                    return@addOnSuccessListener
                }

                binding.tvEmptyState.visibility = android.view.View.GONE

                val badgesList = mutableListOf<Badge>()

                for (document in documents) {
                    val badge = Badge(
                        id = document.id,
                        name = document.getString("name") ?: "Badge",
                        description = document.getString("description") ?: "",
                        icon = document.getString("icon") ?: "🏆",
                        earnedDate = document.getString("earnedDate") ?: "",
                        category = document.getString("category") ?: "General"
                    )
                    badgesList.add(badge)
                }

                // Update badge count
                binding.tvBadgeCount.text = "${badgesList.size} Badges Earned"

                // TODO: Setup RecyclerView Adapter
                // binding.rvBadges.adapter = BadgesAdapter(badgesList)

                Toast.makeText(this, "You have ${badgesList.size} badges!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "Failed to load badges: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

// Data class for Badge
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val earnedDate: String,
    val category: String
)