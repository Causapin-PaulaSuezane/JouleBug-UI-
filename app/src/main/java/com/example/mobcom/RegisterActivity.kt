package com.example.mobcom

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mobcom.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fullText = "ALREADY HAVE AN ACCOUNT? LOG IN"
        val spannable = android.text.SpannableString(fullText)

        val start = fullText.indexOf("LOG IN")
        val end = start + "LOG IN".length

        spannable.setSpan(
            android.text.style.UnderlineSpan(),
            start,
            end,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvLogin.text = spannable

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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
        binding.btnSignUp.setOnClickListener {
            registerUser()
        }

        binding.tvLogin.setOnClickListener {
            finish() // Go back to login
        }
    }

    private fun registerUser() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val contact = binding.etContact.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validation
        if (!validateInputs(fullName, email, password, confirmPassword)) {
            return
        }

        // Clear all errors
        clearErrors()

        // Show loading
        binding.btnSignUp.isEnabled = false
        binding.btnSignUp.text = "CREATING..."

        // Create user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User created successfully, now save additional data to Firestore
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    saveUserDataToFirestore(userId, fullName, email, contact)
                } else {
                    // Registration failed
                    binding.btnSignUp.isEnabled = true
                    binding.btnSignUp.text = "SIGN UP"
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun validateInputs(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {

        // Full Name validation
        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Name required!"
            return false
        }

        if (fullName.length < 3) {
            binding.tilFullName.error = "Name too short!"
            return false
        }

        // Email validation
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email required!"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email!"
            return false
        }

        // Password validation
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password required!"
            return false
        }

        if (password.length < 6) {
            binding.tilPassword.error = "Min 6 characters!"
            return false
        }

        // Confirm password validation
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Confirm password!"
            return false
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords don't match!"
            return false
        }

        return true
    }

    private fun clearErrors() {
        binding.tilFullName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    private fun saveUserDataToFirestore(
        userId: String,
        fullName: String,
        email: String,
        contact: String
    ) {
        // Create user data object
        val userData = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "contact" to contact,
            "level" to 1,
            "xp" to 0,
            "streak" to 0,
            "tasksCompleted" to 0,
            "badgesEarned" to 0,
            "rank" to 0,
            "waterSaved" to 0,
            "co2Reduced" to 0,
            "createdAt" to System.currentTimeMillis()
        )

        // Save to Firestore
        firestore.collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                // Success!
                binding.btnSignUp.isEnabled = true
                binding.btnSignUp.text = "SIGN UP"

                // Navigate back to Login
                navigateToLogin()
            }
            .addOnFailureListener { e ->
                // Failed to save data
                binding.btnSignUp.isEnabled = true
                binding.btnSignUp.text = "SIGN UP"
                Toast.makeText(
                    this,
                    "Failed to save data: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun navigateToLogin() {
        Toast.makeText(this, "Account created! Please login ⚡", Toast.LENGTH_SHORT).show()
        finish() // Goes back to LoginActivity
    }
}