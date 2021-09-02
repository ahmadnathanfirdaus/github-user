package com.ahmad.consumerapp

import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahmad.consumerapp.DatabaseContract.UserColumns.Companion.CONTENT_URI
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "Favorite User"
        rv_favorite.layoutManager = LinearLayoutManager(this)

        val handlerThread = HandlerThread("DataObserver")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        try {
            val myObserver = object : ContentObserver(handler) {
                override fun onChange(self: Boolean) {
                    loadUserAsync()
                }
            }
            contentResolver.registerContentObserver(CONTENT_URI, true, myObserver)
            loadUserAsync()
        } catch (e: Exception) {
            progressBar.visibility = View.INVISIBLE
            no_favorite.visibility = View.VISIBLE
            no_favorite.text = resources.getString(R.string.no_database)
        }
    }

    private fun loadUserAsync() {
        GlobalScope.launch(Dispatchers.Main) {
            val deferredUser = async(Dispatchers.IO) {
                val cursor = contentResolver.query(CONTENT_URI, null, null, null, null)
                MappingHelper.mapCursorToArrayList(cursor)
            }
            val user = deferredUser.await()
            if (user.size > 0) {
                progressBar.visibility = View.INVISIBLE
                adapter = UserAdapter(user)
                rv_favorite.adapter = adapter
            } else {
                progressBar.visibility = View.INVISIBLE
                no_favorite.visibility = View.VISIBLE
            }
        }
    }
}