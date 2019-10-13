package com.example.sdcardhandle


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.sdcardhandle.databinding.FragmentHomeBinding
import com.example.sdcardhandle.viewmodel.StorageViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File
import android.hardware.usb.UsbManager
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.hardware.usb.UsbDevice
import android.content.IntentFilter
import android.app.PendingIntent
import androidx.core.content.ContextCompat.getSystemService


/*https://developer.android.com/guide/topics/connectivity/usb/host*/



class HomeFragment : Fragment() {
    private lateinit var viewModel:StorageViewModel
    private val EXTERNAL_PERMS = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    private val EXTERNAL_REQUEST = 138
    var dstFile:File?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requestForPermission()
        usbConnection()
        viewModel = ViewModelProviders.of(this).get(StorageViewModel::class.java)
        var binding: FragmentHomeBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false)
        binding.viewModel = viewModel
        viewModel.context = context
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_state.setOnClickListener {
            onState()
        }
    }

    private fun onState(){
        val storageDir = File("/storage/")
        searchFile(storageDir)
    }

    private fun searchFile(file:File){
        if(!file.isDirectory()){
            tv_show.append("  " + file.name+"\n")
            /*if(file.name.equals("78024.jpg")){
                dstFile = File(context?.filesDir,file.name)
                file.copyTo(dstFile!!, true, DEFAULT_BUFFER_SIZE)
            }*/
        }else{
            tv_show.append(file.name +"\n")
            var dirList = file.listFiles()
            if(dirList!=null){
                for(i in dirList){
                    searchFile(i)
                }
            }
        }
    }

    private fun usbConnection() {
        var filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        activity?.registerReceiver(mUsbAttachReceiver, filter)
        filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED)
        activity?.registerReceiver(mUsbDetachReceiver, filter)
    }

    var mUsbAttachReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                operationWithUsb(intent)

            }
        }
    }

    private fun operationWithUsb(intent: Intent) {
        val device:UsbDevice? = intent?.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice
        var bytes: ByteArray = ByteArray(1024)
        val TIMEOUT = 0
        val forceClaim = true
        var usbManager = activity?.getSystemService(Context.USB_SERVICE) as UsbManager

        device?.getInterface(0)?.also { intf ->
            intf.getEndpoint(0)?.also { endpoint ->
                usbManager.openDevice(device)?.apply {
                    claimInterface(intf, forceClaim)
                    bulkTransfer(endpoint, bytes, bytes.size, TIMEOUT) //do in another thread
                }
            }
        }

    }

    var mUsbDetachReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                device?.apply {
                    // call your method that cleans up and closes communication with the device
                    tv_status.text = "USB Disconnected"
                }
            }
        }
    }

    fun requestForPermission(): Boolean {
        var isPermissionOn = true
        val version = Build.VERSION.SDK_INT
        if (version >= 23) {
            if (!canAccessExternalSd()) {
                isPermissionOn = false
                requestPermissions(EXTERNAL_PERMS, EXTERNAL_REQUEST)
            }
        }
        return isPermissionOn
    }

    fun canAccessExternalSd(): Boolean {
        return hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun hasPermission(perm: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(activity!!, perm)
    }

    /* Checks if external storage is available for read and write */
    fun isExternalStorageWritable(): Boolean = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    /* Checks if external storage is available to at least read */
    fun isExternalStorageReadable(): Boolean = Environment.getExternalStorageState() in setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.unregisterReceiver(mUsbAttachReceiver)
        activity?.unregisterReceiver(mUsbDetachReceiver)
    }
}
