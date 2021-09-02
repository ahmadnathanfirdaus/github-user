package com.ahmad.githubuser.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ahmad.githubuser.R

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        supportFragmentManager.beginTransaction().add(R.id.setting_holder, PreferenceFragment())
            .commit()
    }
}