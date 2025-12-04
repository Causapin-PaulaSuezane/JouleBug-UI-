package com.example.mobcom

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mobcom.databinding.ActivityHomeBinding
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.animation.Transformation
import android.view.ViewGroup.LayoutParams
import androidx.core.view.isVisible
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize Navigation Drawer
        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView
        navigationView.setNavigationItemSelectedListener(this)

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
        // Menu button - Open Navigation Drawer
        binding.btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Camera Action button
        var isExpanded = false
        binding.cvCameraButton.setOnClickListener {
            if (!isExpanded) {
                expandBottomSection()
                isExpanded = true
            } else {
                collapseBottomSection()
                isExpanded = false
            }
        }

        // Mission section collapse/expand
        binding.headerMission.setOnClickListener {
            toggleDropdown(binding.llMissionContent, binding.ivMissionDropdownArrow)
        }

        // Daily Tasks dropdown
        binding.headerDailyTasks.setOnClickListener {
            toggleDropdown(binding.llDailyTasksContainer, binding.ivDropdownArrow)
        }

        // Weekly Tasks dropdown
        binding.headerWeeklyTasks.setOnClickListener {
            toggleDropdown(binding.llWeeklyTasksContainer, binding.ivWeeklyDropdownArrow)
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

                        // Update drawer header
                        updateDrawerHeader(fullName, level, xp)
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

    private fun updateDrawerHeader(fullName: String, level: Int, xp: Int) {
        val headerView = navigationView.getHeaderView(0)
        val tvDrawerUserName = headerView.findViewById<TextView>(R.id.tvDrawerUserName)
        val tvDrawerXP = headerView.findViewById<TextView>(R.id.tvDrawerXP)

        tvDrawerUserName.text = fullName
        tvDrawerXP.text = "$xp/1000 XP"

        // TODO: Load user avatar
        // val ivDrawerAvatar = headerView.findViewById<ImageView>(R.id.ivDrawerAvatar)
        // Glide.with(this).load(avatarUrl).into(ivDrawerAvatar)
    }

    // Navigation Drawer Item Selection
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                Toast.makeText(this, "Profile (Coming soon!)", Toast.LENGTH_SHORT).show()
                // TODO: startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.nav_leaderboard -> {
                Toast.makeText(this, "Leaderboard (Coming soon!)", Toast.LENGTH_SHORT).show()
                // TODO: startActivity(Intent(this, LeaderboardActivity::class.java))
            }
            R.id.nav_badges -> {
                Toast.makeText(this, "Badges (Coming soon!)", Toast.LENGTH_SHORT).show()
                // TODO: startActivity(Intent(this, BadgesActivity::class.java))
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "Settings (Coming soon!)", Toast.LENGTH_SHORT).show()
                // TODO: startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_about_sdg -> {
                showSDGInfo()
            }
            R.id.nav_logout -> {
                confirmLogout()
            }
        }

        // Close drawer after item selection
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun toggleDropdown(container: View, arrow: View) {
        if (container.isVisible) {
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

    private fun openCamera() {
        Toast.makeText(this, "📸 Opening camera to capture eco-actions! (Coming Soon)", Toast.LENGTH_SHORT).show()
    }

    private fun expandBottomSection() {
        binding.bottomActionSection.animate()
            .translationY(0f)  // Slide to original position (fully visible)
            .setDuration(300)
            .start()

        // Optional: Rotate camera button
        binding.cvCameraButton.animate()
            .rotation(360f)
            .setDuration(300)
            .start()
    }

    private fun collapseBottomSection() {
        binding.bottomActionSection.animate()
            .translationY(250f)  // Slide back down (partially hidden)
            .setDuration(300)
            .start()

        // Optional: Reset rotation
        binding.cvCameraButton.animate()
            .rotation(0f)
            .setDuration(300)
            .start()
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

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}