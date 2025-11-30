package com.example.mobcom

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mobcom.databinding.ActivityHomeBinding
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.animation.Transformation
import android.view.ViewGroup.LayoutParams

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Get current user
        currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            // User not logged in, redirect to login
            navigateToLogin()
            return
        }

        // Load user data
        loadUserData()

        // Setup click listeners
        setupClickListeners()


    }

    private fun setupClickListeners() {
        // Menu button
        binding.btnMenu.setOnClickListener {
            showMenu()
        }

        // Action button
        binding.cvActionButton.setOnClickListener {
            Toast.makeText(this, "Start taking action! 🐞⚡", Toast.LENGTH_SHORT).show()
        }

        // DROPDOWN TOGGLE (Daily Tasks)
        binding.headerDailyTasks.setOnClickListener {
            toggleDropdown(binding.llDailyTasksContainer, binding.ivDropdownArrow)
        }
    }

    private fun loadUserData() {
        currentUserId?.let { userId ->
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get user data
                        val fullName = document.getString("fullName") ?: "Player"
                        val level = document.getLong("level")?.toInt() ?: 1
                        val xp = document.getLong("xp")?.toInt() ?: 0
                        val streak = document.getLong("streak")?.toInt() ?: 0
                        val tasksCompleted = document.getLong("tasksCompleted")?.toInt() ?: 0
                        val badgesEarned = document.getLong("badgesEarned")?.toInt() ?: 0
                        val rank = document.getLong("rank")?.toInt() ?: 0

                        // Update UI
                        updateUI(fullName, level, xp, streak, tasksCompleted, badgesEarned, rank)
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Failed to load data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun updateUI(
        fullName: String,
        level: Int,
        xp: Int,
        streak: Int,
        tasksCompleted: Int,
        badgesEarned: Int,
        rank: Int
    ) {
        // Update greeting
        val firstName = fullName.split(" ").firstOrNull() ?: fullName
        binding.tvGreeting.text = "Hi $firstName!"

        // Update XP
        binding.tvXP.text = xp.toString()

        // Calculate XP percentage (assuming 1000 XP per level)
        binding.pbXP.max = 100
        binding.pbXP.progress = xp.coerceIn(0, 100)

        // Update stats
        binding.tvTasksCompleted.text = tasksCompleted.toString()
        binding.tvBadgesClimbed.text = badgesEarned.toString()
        binding.tvRank.text = if (rank > 0) "#$rank" else "#---"
    }

    private fun showMenu() {
        val menuItems = arrayOf(
            "Profile",
            "Leaderboard",
            "Badges",
            "Settings",
            "About SDG",
            "Logout"
        )

        AlertDialog.Builder(this)
            .setTitle("MENU")
            .setItems(menuItems) { dialog, which ->
                when (which) {
                    0 -> {
                        // Profile
                        Toast.makeText(this, "Profile (Coming soon!)", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        // Leaderboard
                        Toast.makeText(this, "Leaderboard (Coming soon!)", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        // Badges
                        Toast.makeText(this, "Badges (Coming soon!)", Toast.LENGTH_SHORT).show()
                    }
                    3 -> {
                        // Settings
                        Toast.makeText(this, "Settings (Coming soon!)", Toast.LENGTH_SHORT).show()
                    }
                    4 -> {
                        // About SDG
                        showSDGInfo()
                    }
                    5 -> {
                        // Logout
                        confirmLogout()
                    }
                }
            }
            .show()
    }

    private fun toggleDropdown(container: View, arrow: View) {
        if (container.visibility == View.VISIBLE) {
            // Collapse
            collapse(container)
            rotateArrow(arrow, 180f, 0f)
        } else {
            // Expand
            expand(container)
            rotateArrow(arrow, 0f, 180f)
        }
    }

    private fun expand(v: View) {
        v.measure(
            View.MeasureSpec.makeMeasureSpec((v.parent as View).width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.UNSPECIFIED
        )
        val targetHeight = v.measuredHeight

        v.layoutParams.height = 0
        v.visibility = View.VISIBLE

        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                v.layoutParams.height =
                    if (interpolatedTime == 1f) LayoutParams.WRAP_CONTENT
                    else (targetHeight * interpolatedTime).toInt()
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean = true
        }

        a.duration = (targetHeight / v.context.resources.displayMetrics.density).toLong()
        v.startAnimation(a)
    }

    private fun collapse(v: View) {
        val initialHeight = v.measuredHeight

        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                if (interpolatedTime == 1f) {
                    v.visibility = View.GONE
                } else {
                    v.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean = true
        }

        a.duration = (initialHeight / v.context.resources.displayMetrics.density).toLong()
        v.startAnimation(a)
    }

    private fun rotateArrow(arrow: View, from: Float, to: Float) {
        val rotate = RotateAnimation(
            from, to,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotate.duration = 300
        rotate.fillAfter = true
        arrow.startAnimation(rotate)
    }

    private fun showSDGInfo() {
        AlertDialog.Builder(this)
            .setTitle("OUR MISSION 🌍")
            .setMessage(
                "SDG 11: Sustainable Cities and Communities\n" +
                        "Making cities inclusive, safe, resilient and sustainable.\n\n" +
                        "SDG 13: Climate Action\n" +
                        "Taking urgent action to combat climate change and its impacts.\n\n" +
                        "Together, we're building a better future through daily eco-actions!"
            )
            .setPositiveButton("GOT IT!") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("LOGOUT?")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("YES") { _, _ ->
                logout()
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(this, "See you soon! ⚡", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}