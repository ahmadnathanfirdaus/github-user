package com.ahmad.consumerapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GithubUser(
    var username: String? = null,
    var avatar: String? = null,
    var url: String? = null,
    var isFavorite: Boolean = false
) : Parcelable