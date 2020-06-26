package com.paru.todo

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.paru.todo.DTO.ToDo
import com.paru.todo.DTO.ToDoItem
import kotlinx.android.synthetic.main.activity_item.*

class ItemActivity : AppCompatActivity() {

    lateinit var dbHandler:DBHandler
    var todoId:Long=-1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        setSupportActionBar(item_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        supportActionBar?.title = intent.getStringExtra(INTENT_TODO_NAME)

        todoId=intent.getLongExtra(INTENT_TODO_ID,-1)
        dbHandler=DBHandler(this)
        rv_item.layoutManager=LinearLayoutManager(this)

        fab_item.setOnClickListener{
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Add Item")
            val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
            val toDoName = view.findViewById<EditText>(R.id.ev_todo)
            dialog.setView(view)
            dialog.setPositiveButton("Add") { _: DialogInterface, _: Int ->
                if (toDoName.text.isNotEmpty()) {
                    val item = ToDoItem()
                    item.itemName = toDoName.text.toString()
                    item.toDoId = todoId
                    item.isCompleted = false
                    dbHandler.addToDoItem(item)
                    refreshList()
                }
            }
            dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->

            }
            dialog.show()
        }
    }

    fun updateItem(item :ToDoItem ){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Update Item")
        val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
        val toDoName = view.findViewById<EditText>(R.id.ev_todo)
        toDoName.setText(item.itemName)
        dialog.setView(view)
        dialog.setPositiveButton("Update") { _: DialogInterface, _: Int ->
            if (toDoName.text.isNotEmpty()) {
                item.itemName = toDoName.text.toString()
                item.toDoId = todoId
                item.isCompleted = false
                dbHandler.updateToDoItem(item)
                refreshList()
            }
        }
        dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->

        }
        dialog.show()
    }

    override fun onResume() {

        refreshList()
        super.onResume()
    }

    private fun refreshList() {
        rv_item.adapter = ItemAdapter(this,dbHandler.getToDoItems(todoId))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item?.itemId == android.R.id.home) {
            finish()
            true
        } else
            super.onOptionsItemSelected(item)
    }
    class ItemAdapter(val activity: ItemActivity, val list: MutableList<ToDoItem>) :
        RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_singlerow, p0, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
            holder.itemName.text = list[p1].itemName
            holder.itemName.isChecked = list[p1].isCompleted
            holder.itemName.setOnClickListener {
                list[p1].isCompleted = !list[p1].isCompleted
                activity.dbHandler.updateToDoItem(list[p1])
            }

            holder.delete.setOnClickListener{
                val dialog = AlertDialog.Builder(activity)
                dialog.setTitle("Are you sure")
                dialog.setMessage("Do you want to delete this item ?")
                dialog.setPositiveButton("Continue") { _: DialogInterface, _: Int ->
                    activity.dbHandler.deleteToDoItem(list[p1].id)
                    activity.refreshList()
                }
                dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->

                }
                dialog.show()
            }

            holder.edit.setOnClickListener{
                activity.updateItem(list[p1])
            }
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val itemName: CheckBox = v.findViewById(R.id.cv_item)
            val edit:ImageView=v.findViewById(R.id.iv_edit)
            val delete:ImageView=v.findViewById(R.id.iv_delete)
        }
    }
}