package com.example.todolist

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

class RegistrationActivity : AppCompatActivity() {

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var register: Button
    private lateinit var login: TextView

    private lateinit var mAuth:FirebaseAuth
    private lateinit var loader: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_registration)

        toolbar=findViewById(R.id.RegistrationToolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Registration"

        mAuth = FirebaseAuth.getInstance()
        loader = ProgressDialog(this)

        email=findViewById(R.id.RegistrationEmail)
        password=findViewById(R.id.RegistrationPassword)
        confirmPassword=findViewById(R.id.ConfirmRegistrationPassword)
        register=findViewById(R.id.RegistrationButton)
        login=findViewById(R.id.RegistrationPageQuestion)

        login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        register.setOnClickListener {
            var emailText: String = email.text.toString().trim()
            var passwordText: String = password.text.toString().trim()

            if (TextUtils.isEmpty(emailText)){
                email.error = "Email is required"
                return@setOnClickListener
            }
            if (passwordText!=confirmPassword.text.toString().trim()){
                Toast.makeText(this, "Password does not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(passwordText)){
                password.error = "Password is required"
                return@setOnClickListener
            }else{
                loader.setMessage("Registration in progress")
                loader.setCanceledOnTouchOutside(false)
                loader.show()
                mAuth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener {
                    if (it.isSuccessful) {
                        var intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val error = it.exception.toString()
                        Toast.makeText(this, "Registration failed $error", Toast.LENGTH_SHORT)
                            .show()
                        Log.i("tag",error)
                        loader.dismiss()
                    }

                }
            }



        }
    }
}