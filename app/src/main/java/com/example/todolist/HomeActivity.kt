package com.example.todolist

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.DateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var floatingActionButton: FloatingActionButton

    private lateinit var reference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUser: FirebaseUser
    private lateinit var onlineUserID: String

    private lateinit var loader: ProgressDialog

    private lateinit var key: String
    private lateinit var task: String
    private lateinit var description: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        toolbar = findViewById(R.id.homeToolBar)
        floatingActionButton = findViewById(R.id.fab)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Todo List App"

        recyclerView = findViewById(R.id.recyclerView)
        var linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = linearLayoutManager

        loader = ProgressDialog(this)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser!!
        onlineUserID = mUser.uid
        reference = FirebaseDatabase.getInstance().reference.child("tasks").child(onlineUserID)

        floatingActionButton.setOnClickListener {
            addTask()
        }
    }

    private fun addTask() {
        var myDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = LayoutInflater.from(this)

        var myView: View = inflater.inflate(R.layout.input_file, null)
        myDialog.setView(myView)

        val dialog:AlertDialog = myDialog.create()
        dialog.setCancelable(false)

        val task: EditText = myView.findViewById(R.id.task)
        val description: EditText = myView.findViewById(R.id.description)
        var save: Button = myView.findViewById(R.id.saveButton)
        var cancel: Button = myView.findViewById(R.id.cancelButton)

        cancel.setOnClickListener { dialog.dismiss() }
        save.setOnClickListener {
            var mTask: String = task.text.toString().trim()
            var mDescription: String = description.text.toString().trim()
            var id: String = reference.push().key!!
            var date: String = DateFormat.getDateInstance().format(Date())

            if (TextUtils.isEmpty(mTask)){
                task.error = "Task Required"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(mDescription)){
                description.error = "Description Required"
                return@setOnClickListener
            }else{
                loader.setMessage("Adding your data")
                loader.setCanceledOnTouchOutside(false)
                loader.show()

                var model = Model(mTask, mDescription, id, date)
                reference.child(id).setValue(model).addOnCompleteListener{
                    if (it.isSuccessful){
                        Toast.makeText(this, "Task has been inserted successfully", Toast.LENGTH_SHORT).show()
                        loader.dismiss()
                    }else{
                        val error = it.exception.toString()
                        Toast.makeText(this, "Failed: $error", Toast.LENGTH_SHORT).show()
                        loader.dismiss()
                    }
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onStart() {
        super.onStart()

        var options: FirebaseRecyclerOptions<Model> = FirebaseRecyclerOptions.Builder<Model>()
            .setQuery(reference, Model::class.java)
            .build()
        var adapter: FirebaseRecyclerAdapter<Model, MyViewHolder> = object: FirebaseRecyclerAdapter<Model, MyViewHolder>(options){
            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: Model) {
                holder.setDate(model.getDate())
                holder.setTask(model.getTask())
                holder.setDesc(model.getDescription())

                holder.mView.setOnClickListener {
                    key = getRef(position).key.toString()
                    task = model.getTask()
                    description = model.getDescription()

                    updateTask()
                }

            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                var view:View = LayoutInflater.from(parent.context).inflate(R.layout.retrieved_layout, parent, false)
                return MyViewHolder(view)
            }
        }
        recyclerView.adapter = adapter
        adapter.startListening()
    }

    class MyViewHolder: RecyclerView.ViewHolder {
        var mView:View = itemView

        constructor(@NonNull itemView: View) : super(itemView)

        fun setTask(task: String){
            var taskTextView: TextView = mView.findViewById(R.id.taskTv)
            taskTextView.text = task
        }

        fun setDesc(desc: String){
            var descTextView: TextView = mView.findViewById(R.id.descriptionTv)
            descTextView.text = desc
        }

        fun setDate(date: String){
            var dateTextView: TextView = mView.findViewById(R.id.dateTv)
            dateTextView.text = date
        }
    }

    private fun updateTask(){
        var myDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = LayoutInflater.from(this)
        var view: View = inflater.inflate(R.layout.update_data, null)
        myDialog.setView(view)

        var dialog = myDialog.create()

        var mTask: EditText = view.findViewById(R.id.mEditTextTask)
        var mDescription: EditText = view.findViewById(R.id.mEditTextDescription)

        mTask.setText(task)
        mTask.setSelection(task.length)

        mDescription.setText(description)
        mDescription.setSelection(description.length)

        var delButton: Button = view.findViewById(R.id.btnDelete)
        var updateButton: Button = view.findViewById(R.id.btnUpdate)

        delButton.setOnClickListener {
            reference.child(key).removeValue().addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show()
                }else{
                    val err = it.exception.toString()
                    Toast.makeText(this, "Failed to delete task: $err", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.dismiss()
        }

        updateButton.setOnClickListener {
            task = mTask.text.toString().trim()
            description = mDescription.text.toString().trim()

            var date = DateFormat.getDateInstance().format(Date())

            var model = Model(task, description, key, date)

            reference.child(key).setValue(model).addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(this, "Data has been updated successfully", Toast.LENGTH_SHORT).show()
                }else{
                    val err: String = it.exception.toString()
                    Toast.makeText(this, "Update failed: $err", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.logout->{
                mAuth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}