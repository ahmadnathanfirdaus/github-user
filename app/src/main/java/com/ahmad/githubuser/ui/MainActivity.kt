package com.ahmad.githubuser.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahmad.githubuser.GithubUser
import com.ahmad.githubuser.R
import com.ahmad.githubuser.adapter.UserAdapter
import com.ahmad.githubuser.db.UserHelper
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var mIntent: Intent
    private lateinit var userHelper: UserHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userHelper = UserHelper.getInstance(applicationContext)
        userHelper.open()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)

        val settings = menu?.findItem(R.id.settings)
        settings?.setOnMenuItemClickListener {
            mIntent = Intent(this, SettingActivity::class.java)
            startActivity(mIntent)
            true
        }

        val favorite = menu?.findItem(R.id.favorite)
        favorite?.setOnMenuItemClickListener {
            mIntent = Intent(this, FavoriteActivity::class.java)
            startActivity(mIntent)
            true
        }

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu?.findItem(R.id.search)?.actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = resources.getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (isOnline(applicationContext)) {
                    getUser(query.toString())
                } else {
                    Toast.makeText(this@MainActivity, "No internet connection", Toast.LENGTH_SHORT)
                        .show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
        return true
    }

    fun getUser(uname: String) {
        home.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE

        val client = AsyncHttpClient()
        val url = "https://api.github.com/search/users?q=$uname"
        client.addHeader("User-Agent", "request")
        client.addHeader("Authorization", "9dc2d0455f4ff6d14e8ff2eb3808c6c45fd5c0c6")
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray
            ) {
                progressBar.visibility = View.INVISIBLE
                val listUser = ArrayList<GithubUser>()
                val result = String(responseBody)

                try {
                    val jsonArray = JSONObject(result).getJSONArray("items")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val username = jsonObject.getString("login")
                        val avatar = jsonObject.getString("avatar_url")
                        val url = jsonObject.getString("url")
                        val user = GithubUser(username, avatar, url)
                        listUser.add(user)
                    }
                    rv_result.layoutManager = LinearLayoutManager(this@MainActivity)
                    val adapter = UserAdapter(listUser)
                    rv_result.adapter = adapter
                    adapter.setOnItemClickCallback(object : UserAdapter.OnItemClickCallback {
                        override fun onItemClicked(data: GithubUser) {
                            if (isOnline(applicationContext)) {
                                showDetail(data)
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "No internet connection",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }

                    })
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                progressBar.visibility = View.INVISIBLE
                val errorMessage = when (statusCode) {
                    401 -> "$statusCode : Bad Request"
                    403 -> "$statusCode : Forbidden"
                    404 -> "$statusCode : Not Found"
                    else -> "$statusCode : ${error?.message}"
                }
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showDetail(data: GithubUser) {
        val isFavorite = userHelper.isFavorite(data.username.toString())
        mIntent = Intent(this, DetailActivity::class.java)
        mIntent.putExtra(DetailActivity.IS_FAVORITE, isFavorite)
        mIntent.putExtra(DetailActivity.EXTRA_USER, data)
        startActivity(mIntent)
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