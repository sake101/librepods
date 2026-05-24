package me.kavishdevar.librepods.shizuku

import android.content.pm.PackageManager
import android.hardware.input.IInputManager
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

object ShizukuInputInjector {

    private const val TAG = "ShizukuInputInjector"

    val isAvailable: Boolean
        get() = try {
            Shizuku.getBinder() != null
        } catch (_: Exception) {
            false
        }

    val hasPermission: Boolean
        get() = isAvailable &&
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    fun requestPermission(requestCode: Int = 1001) {
        if (isAvailable && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(requestCode)
        }
    }

    fun injectVoiceAssistKey(): Boolean {
        if (!hasPermission) return false
        return try {
            val binder = ShizukuBinderWrapper(
                SystemServiceHelper.getSystemService("input")
            )
            val inputManager = IInputManager.Stub.asInterface(binder)
            val now = SystemClock.uptimeMillis()
            val down = KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOICE_ASSIST, 0)
            val up = KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOICE_ASSIST, 0)
            val ok = inputManager.injectInputEvent(down, 0) && inputManager.injectInputEvent(up, 0)
            Log.d(TAG, "injectVoiceAssistKey: $ok")
            ok
        } catch (e: Exception) {
            Log.e(TAG, "injectVoiceAssistKey failed", e)
            false
        }
    }
}
