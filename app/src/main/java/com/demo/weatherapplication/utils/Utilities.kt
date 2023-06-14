package com.demo.weatherapplication.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.text.Html
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.annotation.RequiresApi
import com.demo.weatherapplication.R
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId

object Utilities {

    fun isFirstTimeLaunch(activity: Context,): Boolean {
        val sharedPreferences = activity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)
        if (isFirstTime) {
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstTime", false)
            editor.apply()
        }
        return isFirstTime
    }

    fun setSp(activity: Context, key: String, value: Any) {
        var prefs: SharedPreferences? = activity.getSharedPreferences(
            activity.packageName,
            Activity.MODE_PRIVATE
        )
        var editor: SharedPreferences.Editor? = prefs!!.edit()
        when (value) {
            is String -> editor!!.putString(key, value)
            is Boolean -> editor!!.putBoolean(key, value)
            is Int -> editor!!.putInt(key, value)
            is Long -> editor!!.putLong(key, value)
            is Float -> editor!!.putFloat(key, value)
        }
        editor!!.commit()
        editor = null
        prefs = null
    }

    fun getSp(activity: Context?, key: String, defaultValue: Any): Any {
        val prefs = activity!!.getSharedPreferences(
            activity.packageName, Activity.MODE_PRIVATE
        )
        return when (defaultValue) {
            is String -> prefs.getString(key, defaultValue)!!
            is Boolean -> prefs.getBoolean(key, defaultValue)
            is Int -> prefs.getInt(key, defaultValue)
            is Long -> prefs.getLong(key, defaultValue)
            else -> prefs.getFloat(key, defaultValue as Float)
        }
    }

    fun clearSp(activity: Context) {
        var prefs: SharedPreferences? =
            activity.getSharedPreferences(activity.packageName, Activity.MODE_PRIVATE)
        var editor: SharedPreferences.Editor? = prefs!!.edit()
        editor!!.clear()
        editor.commit()
        editor = null
        prefs = null
    }


    fun showMessageOK(
        context: Activity,
        title: String,
        message: String,
        okListener: DialogInterface.OnClickListener?
    ) {
        val builder = AlertDialog.Builder(context)
            .setTitle(Html.fromHtml("<b>" + title + "</b>"))
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(context.resources.getString(R.string.common_ok), okListener)
        if (TextUtils.isEmpty(title)) {
            builder.setTitle(title)
        }
        builder.create()
        val dialog: AlertDialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
    }

    fun isInternetAvailable(context: Context?): Boolean {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun ts2td(ts: Long): String {
        val localTime = ts.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }
        return localTime.toString()
    }

    fun k2c(t: Double): Double {
        var intTemp = t
        intTemp = intTemp.minus(273)
        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }


}