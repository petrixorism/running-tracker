package uz.ravshanbaxranov.util

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import java.text.DecimalFormat

//fun showLog(msg: Any, tag: String = "TAGDF") {
//    Log.d(tag, msg.toString())
//}

fun shareResult(context: Context, time: String, distance: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "I went $distance in $time")
    }
    context.startActivity(shareIntent)
}

fun decimalFormat(number: Any): String {
    return DecimalFormat("##.##").format(number)
}



//fun makeUiQuadratic(myView: View){
//
//    myView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//        override fun onGlobalLayout() {
//            // Remove the listener to avoid multiple calls
//            myView.viewTreeObserver.removeOnGlobalLayoutListener(this)
//
//            // Get the view's width
//            val width = myView.width
//
//            // Set the height to be the same as the width
//            myView.layoutParams.height = width
//        }
//    })
//}