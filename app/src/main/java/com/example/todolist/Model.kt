package com.example.todolist

class Model {

    private lateinit var task: String
    private lateinit var description: String
    private lateinit var id: String
    private lateinit var date: String

    constructor()

    constructor(task: String, description: String, id: String, date: String){
        this.task = task
        this.description = description
        this.id = id
        this.date = date
    }

    fun getTask():String{
        return this.task
    }

    fun setTask(task: String){
        this.task = task
    }

    fun getDescription(): String{
        return this.description
    }

    fun setDescription(){

    }

    fun getId(): String{
        return this.id
    }

    fun setId(id: String){
        this.id = id
    }

    fun getDate(): String{
        return this.date
    }

    fun setDate(date: String){
        this.date = date
    }
}