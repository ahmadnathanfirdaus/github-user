package com.ahmad.githubuser.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.ahmad.githubuser.GithubUser
import com.ahmad.githubuser.R
import com.ahmad.githubuser.adapter.SectionsPagerAdapter
import com.ahmad.githubuser.db.DatabaseContract
import com.ahmad.githubuser.db.DatabaseContract.UserColumns.Companion.CONTENT_URI
import com.ahmad.githubuser.db.UserHelper
import com.bumptech.glide.Glide
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_detail.*
import org.json.JSONObject

class DetailActivity : AppCompatActivity() {

    private lateinit var user: GithubUser
    private lateinit var userHelper: UserHelper
    private var isFavorite = false

    companion object {
        const val EXTRA_USER = "user"
        const val IS_FAVORITE = "is_favorite"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        userHelper = UserHelper.getInstance(applicationContext)

        isFavorite = intent.getBooleanExtra(IS_FAVORITE, false)
        user = intent.getParcelableExtra(EXTRA_USER)!!
        val url = user.url

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, url)
        view_pager.adapter = sectionsPagerAdapter
        tabs.setupWithViewPager(view_pager)

        supportActionBar?.title = user.username
        if (isOnline(applicationContext)) {
            getDetail(url.toString())
        } else {
            Toast.makeText(this@DetailActivity, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.detail_options_menu, menu)
        val favorite = menu?.findItem(R.id.favorite)
        if (isFavorite) {
            favorite?.icon =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_favorite_24, theme)
        } else {
            favorite?.icon = ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_baseline_favorite_border_24,
                theme
            )
        }
        favorite?.setOnMenuItemClickListener { item ->
            isFavorite = !isFavorite
            setFavoriteState(isFavorite, item)
            true
        }

        val share = menu?.findItem(R.id.share)
        share?.setOnMenuItemClickListener {
            val username = user.username
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Find out $username at https://github.com/$username")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
            true
        }
        return true
    }

    fun getDetail(url: String) {
        detail.visibility = View.INVISIBLE
        val client = AsyncHttpClient()
        client.addHeader("User-Agent", "request")
        client.addHeader("Authorization", "9dc2d0455f4ff6d14e8ff2eb3808c6c45fd5c0c6")
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray
            ) {
                detail.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
                val result = String(responseBody)

                try {
                    val jsonObject = JSONObject(result)
                    val username = jsonObject.getString("login")
                    val avatar = jsonObject.getString("avatar_url")
                    val company = jsonObject.getString("company")
                    val location = jsonObject.getString("location")
                    text_username.text = username
                    if (company == "null") text_company.visibility = View.GONE
                    else text_company.text = company
                    if (location == "null") text_location.visibility = View.GONE
                    else text_location.text = location
                    Glide.with(applicationContext)
                        .load(avatar)
                        .into(img_user)
                } catch (e: Exception) {
                    Toast.makeText(this@DetailActivity, e.message, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                val errorMessage = when (statusCode) {
                    401 -> "$statusCode : Bad Request"
                    403 -> "$statusCode : Forbidden"
                    404 -> "$statusCode : Not Found"
                    else -> "$statusCode : ${error?.message}"
                }
                Toast.makeText(this@DetailActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun setFavoriteState(state: Boolean, item: MenuItem) {
        val values = ContentValues()
        if (state) {
            values.put(DatabaseContract.UserColumns.USERNAME, user.username)
            values.put(DatabaseContract.UserColumns.AVATAR, user.avatar)
            values.put(DatabaseContract.UserColumns.URL, user.url)
            contentResolver.insert(CONTENT_URI, values)
            item.icon =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_favorite_24, theme)
            Toast.makeText(this, "Added to favorite", Toast.LENGTH_SHORT).show()
        } else {
            contentResolver.delete(CONTENT_URI, user.username, null)
            item.icon = ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_baseline_favorite_border_24,
                theme
            )
            Toast.makeText(this, "Removed from favorite", Toast.LENGTH_SHORT).show()
        }
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