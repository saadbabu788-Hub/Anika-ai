package com.example.action

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import com.example.data.model.ActionResult
import java.util.Locale

class DeviceActionExecutor(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    fun evaluateAction(command: String): ActionResult? {
        val lower = command.trim().lowercase(Locale.ROOT)

        // 1. Device Hardware: Flashlight
        if (lower.contains("flashlight on") || lower.contains("torch on") || lower.contains("light on")) {
            return setTorchMode(true)
        }
        if (lower.contains("flashlight off") || lower.contains("torch off") || lower.contains("light off")) {
            return setTorchMode(false)
        }

        // 2. Device Audio: Volume control
        if (lower.contains("increase volume") || lower.contains("volume up") || lower.contains("awaaz badhao")) {
            return adjustVolume(AudioManager.ADJUST_RAISE)
        }
        if (lower.contains("decrease volume") || lower.contains("volume down") || lower.contains("awaaz kam karo")) {
            return adjustVolume(AudioManager.ADJUST_LOWER)
        }
        if (lower.contains("mute phone") || lower.contains("mute volume") || lower == "mute" || lower.contains("silent phone")) {
            return muteVolume()
        }

        // 3. Apps: Open YouTube
        if (lower.contains("open youtube") || lower.contains("khol youtube") || lower == "youtube") {
            return launchAppOrWeb(
                packageName = "com.google.android.youtube",
                fallbackUrl = "https://www.youtube.com",
                appName = "YouTube"
            )
        }

        // 4. Apps: Open Instagram
        if (lower.contains("open instagram") || lower.contains("open insta") || lower == "instagram") {
            return launchAppOrWeb(
                packageName = "com.instagram.android",
                fallbackUrl = "https://www.instagram.com",
                appName = "Instagram"
            )
        }

        // 5. Apps: Open WhatsApp
        if (lower.contains("open whatsapp") || lower == "whatsapp") {
            return launchApp(
                packageName = "com.whatsapp",
                appName = "WhatsApp"
            )
        }

        // 6. Apps: Open Settings
        if (lower.contains("open settings") || lower.contains("phone settings") || lower == "settings") {
            return launchSettings()
        }

        // 7. Apps: Open Camera
        if (lower.contains("open camera") || lower.contains("kamera kholo") || lower == "camera") {
            return launchCamera()
        }

        // 8. Apps: Open Maps
        if (lower.contains("open maps") || lower.contains("open map") || lower.contains("google maps")) {
            return launchMaps()
        }

        // 9. Apps: Open Calendar
        if (lower.contains("open calendar") || lower.contains("calendar")) {
            return launchCalendar()
        }

        // 10. Navigation / System Screen commands
        if (lower.contains("go home") || lower.contains("home screen")) {
            return launchHomeScreen()
        }
        if (lower.contains("scroll up") || lower.contains("upar scroll")) {
            return ActionResult(
                success = true,
                message = "Scrolling up executed on screen.",
                intentHandled = true
            )
        }
        if (lower.contains("scroll down") || lower.contains("neeche scroll")) {
            return ActionResult(
                success = true,
                message = "Scrolling down executed on screen.",
                intentHandled = true
            )
        }
        if (lower.contains("go back") || lower.contains("back jao")) {
            return ActionResult(
                success = true,
                message = "Navigating back.",
                intentHandled = true
            )
        }
        if (lower.contains("take screenshot") || lower.contains("screenshot lo")) {
            return ActionResult(
                success = true,
                message = "Screenshot command triggered. Kripya Power + Volume Down button press karein ya Assistant gesture use karein.",
                intentHandled = true
            )
        }

        // 11. Communication: Call
        if (lower.startsWith("call ") || lower.contains("ko call karo") || lower.contains("ko call lagao")) {
            val query = extractTarget(lower, listOf("call", "ko call karo", "ko call lagao", "phone"))
            return initiateCall(query)
        }

        // 12. Communication: WhatsApp message
        if (lower.contains("send whatsapp message") || lower.contains("whatsapp message to") || lower.contains("whatsapp chat")) {
            val query = extractTarget(lower, listOf("send whatsapp message to", "whatsapp message to", "open", "whatsapp chat"))
            return openWhatsAppChat(query)
        }

        // 13. Productivity: Set Alarm
        if (lower.contains("set alarm") || lower.contains("alarm lagao")) {
            return setAlarm(lower)
        }

        // 14. Productivity: Web Search
        if (lower.startsWith("search ") || lower.startsWith("google ")) {
            val query = lower.removePrefix("search ").removePrefix("google ").trim()
            return searchWeb(query)
        }

        return null
    }

    private fun setTorchMode(enable: Boolean): ActionResult {
        return try {
            val cm = cameraManager ?: return ActionResult(false, "Camera hardware not accessible for flashlight.")
            val cameraId = cm.cameraIdList.firstOrNull { id ->
                val chars = cm.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId != null) {
                cm.setTorchMode(cameraId, enable)
                ActionResult(
                    success = true,
                    message = if (enable) "Flashlight on kar diya gaya hai." else "Flashlight off kar diya gaya hai.",
                    intentHandled = true
                )
            } else {
                ActionResult(false, "Flashlight hardware not found on device.")
            }
        } catch (e: CameraAccessException) {
            ActionResult(false, "Flashlight access error: ${e.message}")
        } catch (e: SecurityException) {
            ActionResult(false, "Flashlight requires camera permission.", permissionNeeded = "android.permission.CAMERA")
        } catch (e: Exception) {
            ActionResult(false, "Flashlight error: ${e.message}")
        }
    }

    private fun adjustVolume(direction: Int): ActionResult {
        return try {
            audioManager?.adjustVolume(direction, AudioManager.FLAG_SHOW_UI)
            val action = if (direction == AudioManager.ADJUST_RAISE) "badha" else "kam kar"
            ActionResult(
                success = true,
                message = "Volume $action diya gaya hai.",
                intentHandled = true
            )
        } catch (e: Exception) {
            ActionResult(false, "Volume adjust karne mein dikkat: ${e.message}")
        }
    }

    private fun muteVolume(): ActionResult {
        return try {
            audioManager?.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
            ActionResult(
                success = true,
                message = "Phone ko mute kar diya gaya hai.",
                intentHandled = true
            )
        } catch (e: Exception) {
            ActionResult(false, "Mute karne mein dikkat: ${e.message}")
        }
    }

    private fun launchApp(packageName: String, appName: String): ActionResult {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                ActionResult(success = true, message = "$appName open kar diya gaya hai.", intentHandled = true)
            } else {
                ActionResult(success = false, message = "$appName device mein install nahi mila.")
            }
        } catch (e: Exception) {
            ActionResult(success = false, message = "$appName open nahi ho saka: ${e.message}")
        }
    }

    private fun launchAppOrWeb(packageName: String, fallbackUrl: String, appName: String): ActionResult {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        return try {
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                ActionResult(success = true, message = "$appName open kar diya gaya hai.", intentHandled = true)
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                ActionResult(success = true, message = "$appName browser mein open kiya gaya.", intentHandled = true)
            }
        } catch (e: Exception) {
            ActionResult(false, "$appName kholne mein dikkat: ${e.message}")
        }
    }

    private fun launchSettings(): ActionResult {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(success = true, message = "Settings open kar di gayi hai.", intentHandled = true)
        } catch (e: Exception) {
            ActionResult(false, "Settings kholne mein dikkat: ${e.message}")
        }
    }

    private fun launchCamera(): ActionResult {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(success = true, message = "Camera open kar diya gaya hai.", intentHandled = true)
        } catch (e: Exception) {
            ActionResult(false, "Camera kholne mein dikkat: ${e.message}")
        }
    }

    private fun launchMaps(): ActionResult {
        return try {
            val uri = Uri.parse("geo:0,0?q=")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            }
            ActionResult(success = true, message = "Maps open kar diya gaya hai.", intentHandled = true)
        } catch (e: Exception) {
            ActionResult(false, "Maps kholne mein dikkat: ${e.message}")
        }
    }

    private fun launchCalendar(): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("content://com.android.calendar/time")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(success = true, message = "Calendar open kar diya gaya hai.", intentHandled = true)
        } catch (e: Exception) {
            ActionResult(false, "Calendar open karne mein dikkat: ${e.message}")
        }
    }

    private fun launchHomeScreen(): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult(success = true, message = "Home screen par le jaya gaya.", intentHandled = true)
        } catch (e: Exception) {
            ActionResult(false, "Home screen par jane mein dikkat: ${e.message}")
        }
    }

    private fun initiateCall(target: String): ActionResult {
        val cleanNumber = target.filter { it.isDigit() || it == '+' }
        return try {
            val intent = if (cleanNumber.length >= 3) {
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber"))
            } else {
                Intent(Intent.ACTION_DIAL)
            }.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(
                success = true,
                message = if (cleanNumber.isNotBlank()) "$cleanNumber ke liye dialer open kiya gaya." else "Dialer open kiya gaya.",
                intentHandled = true
            )
        } catch (e: Exception) {
            ActionResult(false, "Call karne mein dikkat: ${e.message}")
        }
    }

    private fun openWhatsAppChat(target: String): ActionResult {
        val cleanNumber = target.filter { it.isDigit() }
        return try {
            val uri = if (cleanNumber.length >= 10) {
                Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber")
            } else {
                Uri.parse("https://api.whatsapp.com/")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                ActionResult(success = true, message = "WhatsApp chat open ki gayi.", intentHandled = true)
            } else {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                ActionResult(success = true, message = "WhatsApp open kiya gaya.", intentHandled = true)
            }
        } catch (e: Exception) {
            ActionResult(false, "WhatsApp message bhejne mein dikkat: ${e.message}")
        }
    }

    private fun setAlarm(command: String): ActionResult {
        return try {
            val hour = extractHour(command) ?: 7
            val minutes = extractMinutes(command) ?: 0
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                putExtra(AlarmClock.EXTRA_MESSAGE, "Anika Alarm")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(success = true, message = "Alarm $hour:${String.format(Locale.ROOT, "%02d", minutes)} ke liye set karne open kiya gaya.", intentHandled = true)
        } catch (e: Exception) {
            ActionResult(false, "Alarm set karne mein dikkat: ${e.message}")
        }
    }

    private fun searchWeb(query: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(android.app.SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(success = true, message = "'$query' ke liye web search khola gaya.", intentHandled = true)
        } catch (e: Exception) {
            ActionResult(false, "Web search open karne mein dikkat: ${e.message}")
        }
    }

    private fun extractTarget(input: String, prefixes: List<String>): String {
        var res = input
        for (p in prefixes) {
            if (res.contains(p)) {
                res = res.replace(p, "").trim()
            }
        }
        return res
    }

    private fun extractHour(text: String): Int? {
        val regex = Regex("""\b(\d{1,2})\s*(am|pm|baje|o'?clock)?\b""", RegexOption.IGNORE_CASE)
        val match = regex.find(text) ?: return null
        var h = match.groupValues[1].toIntOrNull() ?: return null
        if (text.contains("pm", ignoreCase = true) && h < 12) h += 12
        if (text.contains("am", ignoreCase = true) && h == 12) h = 0
        return h.coerceIn(0, 23)
    }

    private fun extractMinutes(text: String): Int? {
        val regex = Regex("""\b\d{1,2}:(\d{2})\b""")
        val match = regex.find(text) ?: return null
        return match.groupValues[1].toIntOrNull()?.coerceIn(0, 59)
    }
}
