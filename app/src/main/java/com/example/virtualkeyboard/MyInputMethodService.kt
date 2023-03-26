package com.example.virtualkeyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.media.AudioManager
import android.os.Vibrator
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputConnection
import com.vdurmont.emoji.EmojiManager
import java.util.*

class MyInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {
    private var caps = false
    private var ibibio_keyboard: Keyboard? = null
    private var symbols_keyboard: Keyboard? = null
    private var emojis_keyboard: Keyboard? = null
    private var qwerty_keyboard: Keyboard? = null
    private var keyboardView: CustomKeyboardView? = null
    private var am: AudioManager? = null
    private var v: Vibrator? = null
    override fun onCreateInputView(): View {
        // Creating all Keyboard members
        ibibio_keyboard = Keyboard(this, R.xml.ibibio_keyboard)
        symbols_keyboard = Keyboard(this, R.xml.symbols)
        emojis_keyboard = Keyboard(this, R.xml.emoji)
        qwerty_keyboard = Keyboard(this, R.xml.qwerty)
        keyboardView = getLayoutInflater().inflate(R.layout.keyboard_view, null) as CustomKeyboardView?
        //Send ID of querty Kewboard to KeyboardView
        //It is needed there to prevent drawing red markers (like on IGO keyboard) on keys of standard English QWERTY keyboard
        keyboardView!!.setIdOfQwertyKeyboard(qwerty_keyboard.toString())
        keyboardView!!.keyboard = ibibio_keyboard
        keyboardView!!.setOnKeyboardActionListener(this)
        return keyboardView!!
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        playSound(primaryCode)
        val ic: InputConnection = getCurrentInputConnection() ?: return
        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> {
                val selectedText: CharSequence = ic.getSelectedText(0)
                //Check if there is selection
                if (TextUtils.isEmpty(selectedText)) {
                    //If there is a Emoji before Cursor, 2 characters have to be deleted as Emojis contain 2 characters
                    //If not, just delete 1 character before Cursor
                    if (EmojiManager.isEmoji(
                            getCurrentInputConnection().getTextBeforeCursor(2, 0).toString()
                        )
                    ) {
                        ic.deleteSurroundingText(2, 0)
                    } else {
                        ic.deleteSurroundingText(1, 0)
                    }
                } else {
                    // delete the selection
                    ic.commitText("", 1)
                }
            }
            Keyboard.KEYCODE_SHIFT -> {
                caps = !caps
                //keyboard.setShifted(caps);
                keyboardView!!.invalidateAllKeys()
                keyboardView!!.changeCaps(caps)
            }
            Keyboard.KEYCODE_MODE_CHANGE -> if (keyboardView != null) {
                val current: Keyboard = keyboardView!!.keyboard
                if (current === symbols_keyboard || current === emojis_keyboard) {
                    keyboardView!!.keyboard = ibibio_keyboard
                } else {
                    keyboardView!!.keyboard = symbols_keyboard
                }
            }
            9996 -> if (keyboardView != null) {
                val current: Keyboard = keyboardView!!.keyboard
                if (current === symbols_keyboard || current === emojis_keyboard) {
                    keyboardView!!.keyboard = ibibio_keyboard
                } else {
                    keyboardView!!.keyboard = emojis_keyboard
                }
            }
            9980 -> handleSpecialCharacters("ch")
            9981 -> handleSpecialCharacters("kw")
            9982 -> handleSpecialCharacters("kp")
            9983 -> handleSpecialCharacters("nw")
            9984 -> handleSpecialCharacters("ny")
            9985 -> handleSpecialCharacters("gb")
            9986 -> handleSpecialCharacters("gh")
            9987 -> handleSpecialCharacters("gw")
            9988 -> handleSpecialCharacters("sh")
            9991 ->                 //choose Keyboard, EN for example
                //InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                //imeManager.showInputMethodPicker();
                if (keyboardView != null) {
                    val current: Keyboard = keyboardView!!.keyboard
                    if (current === qwerty_keyboard) {
                        keyboardView!!.keyboard = ibibio_keyboard
                    } else {
                        keyboardView!!.keyboard = qwerty_keyboard
                    }
                }
            else -> {
                var code = primaryCode.toChar()
                if (Character.isLetter(code)) {
                    if (caps) {
                        code = code.uppercaseChar()
                    }
                    ic.commitText(code.toString(), 1)
                } else {
                    ic.commitText(String(Character.toChars(primaryCode)), 1)
                }
            }
        }
    }

    private fun handleSpecialCharacters(character: String) {
        val ic: InputConnection = getCurrentInputConnection()
        if (caps) {
            ic.commitText(character.uppercase(Locale.getDefault()), 1)
        } else {
            ic.commitText(character.lowercase(Locale.getDefault()), 1)
        }
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
    private fun playSound(keyCode: Int) {
        v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        if (v != null && am != null) {
            v!!.vibrate(20)
            when (keyCode) {
                32 -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
                Keyboard.KEYCODE_DONE, 10 -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN)
                Keyboard.KEYCODE_DELETE -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE)
                else -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
            }
        }
    }
}