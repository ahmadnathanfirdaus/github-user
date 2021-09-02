package com.ahmad.githubuser.adapter

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ahmad.githubuser.R
import com.ahmad.githubuser.ui.FollowFragment

class SectionsPagerAdapter(
    private val context: Context,
    fragmentManager: FragmentManager,
    val url: String?
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val TAB_TITLES = intArrayOf(R.string.followers, R.string.following)

    override fun getCount(): Int {
        return TAB_TITLES.size
    }

    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        when (position) {
            0 -> bundle.putString(FollowFragment.EXTRA_URL, "$url/followers")
            1 -> bundle.putString(FollowFragment.EXTRA_URL, "$url/following")
        }
        val followFragment = FollowFragment()
        followFragment.arguments = bundle
        return followFragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

}