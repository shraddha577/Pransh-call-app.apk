package com.example.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.example.model.DeviceTelemetry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface CommandResult {
  data class Executed(
    val actionTag: String,
    val summary: String,
  ) : CommandResult

  data object NotACommand : CommandResult
}

object DeviceCommandExecutor {

  fun readTelemetry(context: Context): DeviceTelemetry {
    val batteryIntent =
      context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct =
      if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 100

    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging =
      status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL

    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetwork
    val caps = activeNetwork?.let { cm.getNetworkCapabilities(it) }
    val networkLabel =
      when {
        caps == null -> "Offline"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
        else -> "Connected"
      }

    val now = Date()
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
    val dateStr = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(now)

    return DeviceTelemetry(
      batteryPercent = batteryPct,
      isCharging = isCharging,
      networkLabel = networkLabel,
      deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
      androidVersion = Build.VERSION.RELEASE ?: "Unknown",
      localTime = timeStr,
      localDate = dateStr,
    )
  }

  fun tryExecuteCommand(context: Context, rawInput: String): CommandResult {
    val input = rawInput.trim()
    val q = input.lowercase(Locale.getDefault())
    val telemetry = readTelemetry(context)

    return when {
      q.contains("pransh") && (q.contains("open") || q.contains("channel") || q.contains("youtube")) -> {
        val opened =
          launchUrl(context, "https://youtube.com/@pransh_official?si=uWYpV_dCdMN5X-13")
        if (opened) {
          CommandResult.Executed(
            actionTag = "OPEN_CHANNEL",
            summary = "Opening @pransh_official YouTube channel.",
          )
        } else {
          CommandResult.Executed(
            actionTag = "INTENT_UNAVAILABLE",
            summary = "No browser or YouTube app available to open @pransh_official.",
          )
        }
      }

      q.contains("open youtube") || q == "youtube" || q.contains("launch youtube") -> {
        val launched =
          launchPackageOrUrl(context, "com.google.android.youtube", "https://www.youtube.com")
        CommandResult.Executed(
          actionTag = "LAUNCH_YOUTUBE",
          summary =
            if (launched) "Launched YouTube."
            else "Could not find YouTube or a web browser on this device.",
        )
      }

      q.contains("open chrome") || q.contains("open browser") -> {
        val launched = launchPackageOrUrl(context, "com.android.chrome", "https://www.google.com")
        CommandResult.Executed(
          actionTag = "LAUNCH_BROWSER",
          summary = if (launched) "Launched web browser." else "No browser app available.",
        )
      }

      q.contains("wifi") && (q.contains("open") || q.contains("setting")) -> {
        val opened = launchAction(context, Settings.ACTION_WIFI_SETTINGS)
        CommandResult.Executed(
          actionTag = "WIFI_SETTINGS",
          summary = if (opened) "Opened Wi-Fi settings." else "Unable to open Wi-Fi settings.",
        )
      }

      q.contains("bluetooth") && (q.contains("open") || q.contains("setting")) -> {
        val opened = launchAction(context, Settings.ACTION_BLUETOOTH_SETTINGS)
        CommandResult.Executed(
          actionTag = "BLUETOOTH_SETTINGS",
          summary =
            if (opened) "Opened Bluetooth settings." else "Unable to open Bluetooth settings.",
        )
      }

      q.contains("open settings") || q == "settings" -> {
        val opened = launchAction(context, Settings.ACTION_SETTINGS)
        CommandResult.Executed(
          actionTag = "SYSTEM_SETTINGS",
          summary = if (opened) "Opened Android system settings." else "Unable to open settings.",
        )
      }

      q.contains("open camera") || q == "camera" -> {
        val opened = launchAction(context, MediaStore.ACTION_IMAGE_CAPTURE)
        CommandResult.Executed(
          actionTag = "OPEN_CAMERA",
          summary = if (opened) "Launched camera." else "No camera app available on this device.",
        )
      }

      q.startsWith("search ") || q.startsWith("google ") -> {
        val query =
          input
            .removePrefix("search ")
            .removePrefix("Search ")
            .removePrefix("google ")
            .removePrefix("Google ")
            .trim()
        if (query.isNotEmpty()) {
          val url = "https://www.google.com/search?q=${Uri.encode(query)}"
          val opened = launchUrl(context, url)
          CommandResult.Executed(
            actionTag = "WEB_SEARCH",
            summary =
              if (opened) "Searching Google for \"$query\"."
              else "No browser found to search for \"$query\".",
          )
        } else {
          CommandResult.NotACommand
        }
      }

      q.startsWith("call ") || q.startsWith("dial ") -> {
        val number =
          input
            .removePrefix("call ")
            .removePrefix("Call ")
            .removePrefix("dial ")
            .removePrefix("Dial ")
            .trim()
        val intent =
          Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
        val opened = startSafe(context, intent)
        CommandResult.Executed(
          actionTag = "DIAL_PHONE",
          summary =
            if (opened) "Opened dialer for $number."
            else "No dialer app available on this device.",
        )
      }

      q.contains("set timer") -> {
        val minutesMatch = Regex("(\\d+)\\s*min").find(q)
        val secondsMatch = Regex("(\\d+)\\s*sec").find(q)
        val totalSeconds =
          when {
            minutesMatch != null -> (minutesMatch.groupValues[1].toIntOrNull() ?: 1) * 60
            secondsMatch != null -> secondsMatch.groupValues[1].toIntOrNull() ?: 30
            else -> 60
          }
        val intent =
          Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, totalSeconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Nova AI Timer")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
        val opened = startSafe(context, intent)
        CommandResult.Executed(
          actionTag = "SET_TIMER",
          summary =
            if (opened) "Set a ${totalSeconds}s timer in your Clock app."
            else "Clock app does not support timer intents on this device.",
        )
      }

      q.contains("battery") || q.contains("diagnostics") || q.contains("system status") || q == "status" -> {
        val chargeNote = if (telemetry.isCharging) "charging" else "on battery"
        CommandResult.Executed(
          actionTag = "DIAGNOSTICS",
          summary =
            "System diagnostics: Battery ${telemetry.batteryPercent}% ($chargeNote), Network: ${telemetry.networkLabel}, Device: ${telemetry.deviceModel} (Android ${telemetry.androidVersion}).",
        )
      }

      q.contains("what time") || q.contains("current time") || q == "time" || q.contains("what date") -> {
        CommandResult.Executed(
          actionTag = "LOCAL_CLOCK",
          summary = "Local system time is ${telemetry.localTime} on ${telemetry.localDate}.",
        )
      }

      else -> CommandResult.NotACommand
    }
  }

  fun launchUrl(context: Context, url: String): Boolean {
    val intent =
      Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    return startSafe(context, intent)
  }

  private fun launchPackageOrUrl(context: Context, packageName: String, fallbackUrl: String): Boolean {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      if (startSafe(context, launchIntent)) return true
    }
    return launchUrl(context, fallbackUrl)
  }

  private fun launchAction(context: Context, action: String): Boolean {
    val intent = Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    return startSafe(context, intent)
  }

  private fun startSafe(context: Context, intent: Intent): Boolean {
    return try {
      context.startActivity(intent)
      true
    } catch (_: Exception) {
      false
    }
  }
}
