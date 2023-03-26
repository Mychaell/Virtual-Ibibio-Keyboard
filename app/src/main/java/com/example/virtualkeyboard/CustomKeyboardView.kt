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
import android.content.Context
import com.example.virtualkeyboard.MyInputMethodService
import android.view.inputmethod.InputMethodInfo
import android.widget.Toast
import android.inputmethodservice.KeyboardView
import com.example.virtualkeyboard.CustomKeyboardView
import android.os.Build
import android.inputmethodservice.Keyboard
import android.util.TypedValue
import android.content.res.TypedArray
import android.graphics.*
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.media.AudioManager
import android.os.Vibrator
import android.view.inputmethod.InputConnection
import android.text.TextUtils
import android.util.AttributeSet
import java.util.*

class CustomKeyboardView(context: Context, attrs: AttributeSet?) : KeyboardView(
    context, attrs
) {
    private var caps = false
    private var idOfQwertyKeyboard: String? = null
    private var longpress = false
    fun setIdOfQwertyKeyboard(idOfQwertyKeyboard: String?) {
        this.idOfQwertyKeyboard = idOfQwertyKeyboard
    }

    fun changeCaps(caps: Boolean) {
        this.caps = caps
    }

    private fun StringChecker(a: String, b: String?): Boolean {
        return a.equals(b, ignoreCase = true)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!longpress) {
            //Paint for little red symbols indicating Popup Characters
            val paint = Paint()
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = getDefaultTextSize(context) / 2.0.toFloat()
            paint.color = Color.RED

            //Paint for custom Arial Font
            val mPaint = Paint()
            mPaint.textAlign = Paint.Align.CENTER
            mPaint.textSize = getDefaultTextSize(context) * 1.3.toFloat()
            mPaint.color = Color.BLACK

            //Plain white Rectangle to overdraw existing keys (as we want to use custom font)
            val rectPaint = Paint()
            rectPaint.color = Color.WHITE

            //Get Font based on Android Version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mPaint.typeface = context.resources.getFont(R.font.arial)
            } else {
                mPaint.typeface = Typeface.createFromAsset(context.assets, "arial.ttf")
            }
            val keys = keyboard.keys
            for (key in keys) {

                //Overdraw all keys with blank white Rect
                val rect = Rect(key.x, key.y, key.x + key.width, key.y + key.height)
                if (key.label != null) {
                    canvas.drawRect(rect, rectPaint)
                }

                //Draw little red symbol on following keys, but just on IGBO Keyboard
                if (!StringChecker(keyboard.toString(), idOfQwertyKeyboard)) {
                    if (key.label != null) {
                        if (key.label == "e" || key.label == "u" || key.label == "i" || key.label == "o" || key.label == "a") {
                            canvas.drawText(
                                "∞",
                                key.x + (key.width - (key.width * 0.5).toFloat()),
                                (key.y + 30).toFloat(),
                                paint
                            )
                        } else if (key.label == "sh") {
                            canvas.drawText(
                                "s",
                                key.x + (key.width - (key.width * 0.5).toFloat()),
                                (key.y + 30).toFloat(),
                                paint
                            )
                        }
                    }
                } else {
                    if (key.label != null) {
                        canvas.drawRect(rect, rectPaint)
                    }
                }

                //Draw custom font on keys
                if (key.label != null) {
                    var keyLabel = key.label.toString()
                    if (caps && !keyLabel.equals("Space", ignoreCase = true)) {
                        keyLabel = keyLabel.uppercase(Locale.getDefault())
                    }
                    canvas.drawText(
                        keyLabel,
                        (key.x + key.width / 2).toFloat(),
                        (key.y + key.height / 1.5).toFloat(),
                        mPaint
                    )
                }
            }
        } else {
            longpress = false
        }
    }

    override fun onLongPress(popupKey: Keyboard.Key): Boolean {
        longpress = if (popupKey.popupCharacters != null) {
            true
        } else {
            false
        }
        return super.onLongPress(popupKey)
    }

    companion object {
        private fun getDefaultTextSize(context: Context): Int {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.textAppearance, typedValue, true)
            val textSizeAttr = intArrayOf(android.R.attr.textSize)
            val typedArray = context.obtainStyledAttributes(typedValue.data, textSizeAttr)
            val textSize = typedArray.getDimensionPixelSize(0, -1)
            typedArray.recycle()
            return textSize
        }
    }
}