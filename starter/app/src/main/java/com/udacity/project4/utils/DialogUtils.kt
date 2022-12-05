package com.udacity.project4.utils

import android.app.AlertDialog
import android.content.Context

fun showAlertDialog(
    title: String,
    message: String,
    dialogButtonsListener: DialogButtons,
    context: Context,
    positiveBtnTitle:String
):AlertDialog.Builder {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("$title")
    builder.setMessage("$message")

    builder.setPositiveButton(positiveBtnTitle) { dialog, which ->
        dialogButtonsListener.setOnPositiveListener()
    }

    builder.setNegativeButton(android.R.string.cancel) { dialog, which ->
//        dialogButtonsListener.setOnNegativeListener()
        dialog.dismiss()
    }

//    builder.setNeutralButton(android.R.string.cancel) { dialog, which ->
//        dialogButtonsListener.setOnNeutralListener()
//    }
    builder.setCancelable(false)
    return builder
}

interface DialogButtons {
    fun setOnPositiveListener() {

    }

//    fun setOnNegativeListener() {
//
//    }

//    fun setOnNeutralListener() {
//
//    }

}