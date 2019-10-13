package com.example.sdcardhandle.viewmodel

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import java.io.File

class StorageViewModel:ViewModel() {
    var fileName:String?=null
    var context:Context?=null
    var listener:ViewModelListener?=null
    var dstFile:File?=null

    fun searchFile(view: View){

    }

    fun copyFile(view: View){

    }
}