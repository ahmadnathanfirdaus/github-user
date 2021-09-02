package com.ahmad.githubuser.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahmad.githubuser.GithubUser
import com.ahmad.githubuser.R
import com.ahmad.githubuser.adapter.UserAdapter
import com.ahmad.githubuser.db.UserHelper
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_main.progressBar
import kotlinx.android.synthetic.main.fragment_follow.*
import org.json.JSONArray

class FollowFragment : Fragment() {

    private lateinit var userHelper: UserHelper

    companion object {
        var EXTRA_URL = "url"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val url = this.arguments?.getString(EXTRA_URL)
        userHelper = UserHelper(requireContext())
        if (isOnline(requireContext())) {
            getList(url.toString())
        } else {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
        }
        return inflater.inflate(R.layout.fragment_follow, container, false)
    }

    fun getList(url: String?) {
        val client = AsyncHttpClient()
        client.addHeader("User-Agent", "request")
        client.addHeader("Authorization", "9dc2d0455f4ff6d14e8ff2eb3808c6c45fd5c0c6")
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?
            ) {
                progressBar.visibility = View.INVISIBLE
                val listUser = ArrayList<GithubUser>()
                val result = responseBody?.let { String(it) }
                try {
                    val jsonArray = JSONArray(result)

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val username = jsonObject.getString("login")
                        val avatar = jsonObject.getString("avatar_url")
                        val url = jsonObject.getString("url")
                        val user = GithubUser(username, avatar, url)
                        listUser.add(user)
                    }
                    if (listUser.size == 0) {
                        nobody.visibility = View.VISIBLE
                    } else {
                        rv_follow.layoutManager = LinearLayoutManager(context)
                        val adapter = UserAdapter(listUser)
                        rv_follow.adapter = adapter
                        adapter.setOnItemClickCallback(object : UserAdapter.OnItemClickCallback {
                            override fun onItemClicked(data: GithubUser) {
                                if (isOnline(requireContext())) {
                                    showDetail(data)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No internet connection",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        })
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showDetail(data: GithubUser) {
        val isFavorite = userHelper.isFavorite(data.username.toString())
        val intent = Intent(context, DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_USER, data)
        intent.putExtra(DetailActivity.IS_FAVORITE, isFavorite)
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