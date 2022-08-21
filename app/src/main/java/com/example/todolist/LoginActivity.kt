package com.example.todolist

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.*
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var email:EditText
    private lateinit var password:EditText
    private lateinit var login: Button
    private lateinit var register:TextView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var loader: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_login)

        toolbar=findViewById(R.id.loginToolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Login"

        mAuth = FirebaseAuth.getInstance()
        loader = ProgressDialog(this)

        if (mAuth != null){
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        email=findViewById(R.id.loginEmail)
        password=findViewById(R.id.loginPassword)
        login=findViewById(R.id.loginButton)
        register=findViewById(R.id.loginPageQuestion)

        register.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        login.setOnClickListener {
            val emailText: String = email.text.toString().trim()
            val passwordText: String = password.text.toString().trim()

            if(TextUtils.isEmpty(emailText)){
                email.error = "Email is required"
                return@setOnClickListener
            }

            if(TextUtils.isEmpty(passwordText)){
                password.error = "Password is required"
                return@setOnClickListener
            }else{
                loader.setMessage("Login in progress")
                loader.setCanceledOnTouchOutside(false)
                loader.show()

                mAuth.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener {
                    if(it.isSuccessful){
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                        loader.dismiss()
                    }else{
                        val error = it.exception.toString()
                        Toast.makeText(this, "Login failed $error", Toast.LENGTH_SHORT).show()
                        loader.dismiss()
                    }
                }
            }
        }

    }
}