package com.udacity.project4.utils

import android.app.AlertDialog
import android.content.Context

fun showAlertDialogFor(
    title: String,
    message: String,
    dialogButtonsListener: DialogButtons,
    context: Context
) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("$title")
    builder.setMessage("$message")

    builder.setPositiveButton(android.R.string.yes) { dialog, which ->
        dialogButtonsListener.setOnPositiveListener()
    }

    builder.setNegativeButton(android.R.string.no) { dialog, which ->
        dialogButtonsListener.setOnNegativeListener()
    }

//    builder.setNeutralButton(android.R.string.cancel) { dialog, which ->
//        dialogButtonsListener.setOnNeutralListener()
//    }
    builder.show()
}

interface DialogButtons {
    fun setOnPositiveListener() {

    }

    fun setOnNegativeListener() {

    }

//    fun setOnNeutralListener() {
//
//    }

}