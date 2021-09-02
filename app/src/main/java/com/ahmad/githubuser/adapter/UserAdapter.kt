package com.ahmad.githubuser.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ahmad.githubuser.GithubUser
import com.ahmad.githubuser.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_user.view.*

class UserAdapter(val listUser: ArrayList<GithubUser>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(user: GithubUser) {
            with(itemView) {
                Glide.with(itemView.context)
                    .load(user.avatar)
                    .into(img_user)

                text_username.text = user.username

                itemView.setOnClickListener { onItemClickCallback?.onItemClicked(user) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(listUser[position])
    }

    override fun getItemCount(): Int {
        return listUser.size
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: GithubUser)
    }
}