package com.example.mobcom

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobcom.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupUI()
        loadUserProfile()
    }

    private fun setupUI() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Edit profile button
        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit Profile (Coming soon!)", Toast.LENGTH_SHORT).show()
        }

        // Share profile button
        binding.btnShareProfile.setOnClickListener {
            shareProfile()
        }
    }

    private fun loadUserProfile() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val fullName = document.getString("fullName") ?: "Player"
                    val email = document.getString("email") ?: ""
                    val level = document.getLong("level")?.toInt() ?: 1
                    val xp = document.getLong("xp")?.toInt() ?: 0
                    val streak = document.getLong("streak")?.toInt() ?: 0
                    val tasksCompleted = document.getLong("tasksCompleted")?.toInt() ?: 0
                    val badgesEarned = document.getLong("badgesEarned")?.toInt() ?: 0
                    val rank = document.getLong("rank")?.toInt() ?: 0
                    val joinDate = document.getString("joinDate") ?: "Unknown"

                    // Update UI
                    binding.tvProfileName.text = fullName
                    binding.tvProfileEmail.text = email
                    binding.tvLevel.text = "Level $level"
                    binding.tvXP.text = "$xp XP"
                    binding.tvStreak.text = "$streak day streak 🔥"
                    binding.tvTasksCompleted.text = tasksCompleted.toString()
                    binding.tvBadgesEarned.text = badgesEarned.toString()
                    binding.tvRank.text = if (rank > 0) "#$rank" else "Unranked"
                    binding.tvJoinDate.text = "Member since $joinDate"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun shareProfile() {
        val shareText = "Check out my JouleBug profile! 🐞🌍 #EcoWarrior #ClimateAction"
        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = android.content.Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }
}