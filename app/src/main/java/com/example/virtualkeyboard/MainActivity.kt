package com.example.virtualkeyboard

import android.content.SharedPreferences
import com.example.virtualkeyboard.PrefManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.virtualkeyboard.R
import android.widget.CompoundButton
import android.content.Intent
import android.content.pm.PackageManager
import android.content.ComponentName
import com.example.virtualkeyboard.MyInputMethodService
import android.view.inputmethod.InputMethodInfo
import android.widget.Toast
import android.inputmethodservice.KeyboardView
import com.example.virtualkeyboard.CustomKeyboardView
import android.os.Build
import android.graphics.Typeface
import android.inputmethodservice.Keyboard
import android.util.TypedValue
import android.content.res.TypedArray
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.media.AudioManager
import android.os.Vibrator
import android.provider.Settings
import android.view.inputmethod.InputConnection
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Switch

class MainActivity : AppCompatActivity() {
    private var prefManager: PrefManager? = null
    override fun onResume() {
        super.onResume()
        checkPrefs()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // use this to start and trigger a service
        //Intent i= new Intent(this, MyInputMethodService.class);
        //startService(i);
        prefManager = PrefManager(this)
        checkPrefs()
        val switchOnOff = findViewById<Switch>(R.id.switchOnOff)
        switchOnOff.isChecked = prefManager!!.isActivated
        switchOnOff.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (!prefManager!!.isActivated) {
                    enableComponent()
                    prefManager!!.setIsActivated(true)
                }
            } else {
                if (prefManager!!.isActivated) {
                    disableComponent()
                    prefManager!!.setIsActivated(false)
                }
            }
            checkPrefs()
        }
        val btnSelectKeyboard = findViewById<ImageView>(R.id.imageViewKeyboard)
        btnSelectKeyboard.setOnClickListener {
            if (prefManager!!.isAdded && prefManager!!.isActivated) {
                val imeManager =
                    applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imeManager.showInputMethodPicker()
            } else if (!prefManager!!.isAdded && prefManager!!.isActivated) {
                val enableIntent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                enableIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(enableIntent)
            }
        }
    }

    private fun disableComponent() {
        val pm = packageManager
        pm?.setComponentEnabledSetting(
            ComponentName(this, MyInputMethodService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
    }

    private fun enableComponent() {
        val pm = packageManager
        pm?.setComponentEnabledSetting(
            ComponentName(this, MyInputMethodService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }

    // check if our keyboard is enabled as input method
    private val isKeyboardAddedInSettings: Boolean
        private get() {
            val packageLocal = packageName
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethodManager != null) {
                val list = inputMethodManager.enabledInputMethodList

                // check if our keyboard is enabled as input method
                for (inputMethod in list) {
                    val packageName = inputMethod.packageName
                    if (packageName == packageLocal) {
                        return true
                    }
                }
            }
            return false
        }

    private fun checkPrefs() {
        prefManager!!.setAdded(isKeyboardAddedInSettings)
        if (prefManager!!.isActivated && prefManager!!.isAdded) {
            Toast.makeText(applicationContext, "Your keyboard is ready to use.", Toast.LENGTH_SHORT)
                .show()
        } else if (!prefManager!!.isActivated) {
            Toast.makeText(applicationContext, "Your keyboard is disabled.", Toast.LENGTH_SHORT)
                .show()
        } else if (!prefManager!!.isAdded) {
            Toast.makeText(
                applicationContext,
                "Your keyboard is not added or enabled in Android settings yet.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}