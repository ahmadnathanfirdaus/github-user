package com.ahmad.githubuser.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.ahmad.githubuser.AlarmReceiver
import com.ahmad.githubuser.R

class PreferenceFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var REMINDER: String
    private lateinit var isRemindedPreference: SwitchPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        init()
        setSummaries()
    }

    private fun init() {
        REMINDER = resources.getString(R.string.key_reminder)
        isRemindedPreference = findPreference<SwitchPreference>(REMINDER) as SwitchPreference
    }

    private fun setSummaries() {
        val sh = preferenceManager.sharedPreferences
        isRemindedPreference.isChecked = sh.getBoolean(REMINDER, false)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val alarmReceiver = AlarmReceiver()
        if (key == REMINDER) {
            val isReminded = sharedPreferences.getBoolean(REMINDER, false)
            if (isReminded) {
                context?.let { alarmReceiver.setReminder(it) }
            } else {
                context?.let { alarmReceiver.cancelReminder(it) }
            }
        }
    }

}