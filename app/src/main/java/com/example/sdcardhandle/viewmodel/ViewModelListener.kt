package com.example.sdcardhandle.viewmodel

interface ViewModelListener {
    fun onSuccess(msg:String)
    fun onFailure(msg:String)
}