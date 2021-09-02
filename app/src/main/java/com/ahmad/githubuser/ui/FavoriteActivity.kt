package com.ahmad.githubuser.ui

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahmad.githubuser.GithubUser
import com.ahmad.githubuser.MappingHelper
import com.ahmad.githubuser.R
import com.ahmad.githubuser.adapter.UserAdapter
import com.ahmad.githubuser.db.DatabaseContract.UserColumns.Companion.CONTENT_URI
import com.ahmad.githubuser.db.UserHelper
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class FavoriteActivity : AppCompatActivity() {

    private lateinit var userHelper: UserHelper
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        supportActionBar?.title = "Favorite"
        userHelper = UserHelper.getInstance(applicationContext)
        rv_favorite.layoutManager = LinearLayoutManager(this)

        val handlerThread = HandlerThread("DataObserver")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        val myObserver = object : ContentObserver(handler) {
            override fun onChange(self: Boolean) {
                loadUserAsync()
            }
        }
        contentResolver.registerContentObserver(CONTENT_URI, true, myObserver)
        loadUserAsync()
    }

    private fun loadUserAsync() {
        GlobalScope.launch(Dispatchers.Main) {
            val deferredUser = async(Dispatchers.IO) {
                val cursor = contentResolver.query(CONTENT_URI, null, null, null, null)
                MappingHelper.mapCursorToArrayList(cursor)
            }
            val user = deferredUser.await()
            if (user.size > 0) {
                no_favorite.visibility = View.INVISIBLE
                adapter = UserAdapter(user)
                rv_favorite.adapter = adapter
                adapter.setOnItemClickCallback(object : UserAdapter.OnItemClickCallback {
                    override fun onItemClicked(data: GithubUser) {
                        if (isOnline(applicationContext)) {
                            showDetail(data)
                        } else {
                            Toast.makeText(
                                this@FavoriteActivity,
                                "No internet connection",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                })
            }
        }
    }

    fun showDetail(data: GithubUser) {
        val isFavorite = userHelper.isFavorite(data.username.toString())
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(DetailActivity.IS_FAVORITE, isFavorite)
        intent.putExtra(DetailActivity.EXTRA_USER, data)
        startActivity(intent)
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        } else {
            TODO("VERSION.SDK_INT < M")
        }
        if (capabilities != null) {
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    true
                }
                else -> false
            }
        }
        return false
    }
}