package com.ahmad.githubuser.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.ahmad.githubuser.db.DatabaseContract.AUTHORITY
import com.ahmad.githubuser.db.DatabaseContract.UserColumns.Companion.CONTENT_URI
import com.ahmad.githubuser.db.DatabaseContract.UserColumns.Companion.TABLE_NAME
import com.ahmad.githubuser.db.UserHelper

class UserProvider : ContentProvider() {

    companion object {
        private const val USER = 1
        private const val USERNAME = 2
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private lateinit var userHelper: UserHelper

        init {
            uriMatcher.addURI(AUTHORITY, TABLE_NAME, USER)
            uriMatcher.addURI(AUTHORITY, "$TABLE_NAME/#", USERNAME)
        }
    }

    override fun onCreate(): Boolean {
        userHelper = UserHelper.getInstance(context as Context)
        userHelper.open()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val cursor: Cursor?
        when (uriMatcher.match(uri)) {
            USER -> cursor = userHelper.queryAll()
            else -> cursor = null
        }
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        val added: Long = when (USER) {
            uriMatcher.match(uri) -> userHelper.insert(contentValues)
            else -> 0
        }
        context?.contentResolver?.notifyChange(CONTENT_URI, null)
        return Uri.parse("$CONTENT_URI/$added")
    }


    override fun update(
        uri: Uri,
        contentValues: ContentValues?,
        s: String?,
        strings: Array<String>?
    ): Int {
        val updated: Int = when (USERNAME) {
            uriMatcher.match(uri) -> userHelper.update(
                uri.lastPathSegment.toString(),
                contentValues
            )
            else -> 0
        }
        context?.contentResolver?.notifyChange(CONTENT_URI, null)
        return updated
    }

    override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int {
        val deleted: Int = when (USER) {
            uriMatcher.match(uri) -> userHelper.deleteByUsername(s.toString())
            else -> 0
        }
        context?.contentResolver?.notifyChange(CONTENT_URI, null)
        return deleted
    }
}