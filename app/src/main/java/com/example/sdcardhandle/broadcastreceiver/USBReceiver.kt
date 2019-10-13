package com.example.sdcardhandle.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.hardware.usb.UsbManager
import android.hardware.usb.UsbDevice


class USBReceiver: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val device = p1?.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice
        Toast.makeText(p0,"usb connected: ${device.deviceName}",Toast.LENGTH_LONG).show()
    }
}