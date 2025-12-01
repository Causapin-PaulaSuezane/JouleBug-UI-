package com.example.mobcom

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.mobcom.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val text = "NEW PLAYER? JOIN NOW!"
        val spannable = android.text.SpannableString(text)

        val start = text.indexOf("JOIN NOW")
        val end = start + "JOIN NOW".length

        spannable.setSpan(
            android.text.style.UnderlineSpan(),
            start,
            end,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvSignUp.text = spannable

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        if (auth.currentUser != null) {
            navigateToHome()
        }

        animateLogo()

        setupClickListeners()
    }

    private fun animateLogo() {
        // Animate the logo
        val logoAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bounce_animation)
        binding.ivLogo.startAnimation(logoAnimation)

        // Animate the glow effect
        val glowAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
        binding.glowEffect.startAnimation(glowAnimation)
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validation
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email required!"
            return
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password required!"
            return
        }

        // Clear errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // Show loading
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "LOGGING IN..."

        // Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "LOG IN"

                if (task.isSuccessful) {
                    // Login successful
                    Toast.makeText(this, "Welcome back! ⚡", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    // Login failed
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}