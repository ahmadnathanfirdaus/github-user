package com.ahmad.githubuser

import android.database.Cursor
import com.ahmad.githubuser.db.DatabaseContract

object MappingHelper {
    fun mapCursorToArrayList(userCursor: Cursor?): ArrayList<GithubUser> {
        val userList = ArrayList<GithubUser>()

        userCursor?.apply {
            while (moveToNext()) {
                val username =
                    getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.USERNAME))
                val avatar = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.AVATAR))
                val url = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.URL))
                userList.add(GithubUser(username, avatar, url, true))
            }
        }
        return userList
    }
}