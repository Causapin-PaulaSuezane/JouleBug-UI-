package com.example.mobcom

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobcom.databinding.ActivityLeaderboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupUI()
        loadLeaderboard()
    }

    private fun setupUI() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(this)
    }

    private fun loadLeaderboard() {
        binding.progressBar.visibility = android.view.View.VISIBLE

        firestore.collection("users")
            .orderBy("xp", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = android.view.View.GONE

                if (documents.isEmpty) {
                    Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val leaderboardList = mutableListOf<LeaderboardUser>()
                var rank = 1

                for (document in documents) {
                    val user = LeaderboardUser(
                        userId = document.id,
                        name = document.getString("fullName") ?: "Unknown",
                        level = document.getLong("level")?.toInt() ?: 1,
                        xp = document.getLong("xp")?.toInt() ?: 0,
                        rank = rank++
                    )
                    leaderboardList.add(user)
                }

                // TODO: Setup RecyclerView Adapter
                // binding.rvLeaderboard.adapter = LeaderboardAdapter(leaderboardList)

                Toast.makeText(this, "Loaded ${leaderboardList.size} users", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "Failed to load leaderboard: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

// Data class for Leaderboard User
data class LeaderboardUser(
    val userId: String,
    val name: String,
    val level: Int,
    val xp: Int,
    val rank: Int
)