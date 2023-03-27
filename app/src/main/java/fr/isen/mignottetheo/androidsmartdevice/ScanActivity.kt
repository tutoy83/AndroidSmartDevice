package fr.isen.mignottetheo.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.*

import fr.isen.mignottetheo.androidsmartdevice.databinding.ActivityScanBinding



class ScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanBinding
    private var active = 1
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val mHandler = Handler(Looper.getMainLooper()) // Specify Looper for Handler
    private val REQUEST_PERMISSION_BLUETOOTH_CONNECT = 1
    private var mLeDeviceListAdapter: DeviceAdapter? = null
    private val mLeDevices = ArrayList<BluetoothDevice>()

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            //Callback when permissions OK
            if (permissions.all { it.value }) {
                scanDevices()
            }
        }

    private fun addDevice(device: BluetoothDevice) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device)
            mLeDeviceListAdapter?.notifyDataSetChanged()
        }
    }

    companion object {
        private const val TAG = "ScanActivity"
        private const val REQUEST_ENABLE_BT = 1
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        private const val SCAN_PERIOD: Long = 10000 //10 seconds
    }

    // Device scan callback.
    private val mLeScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            addDevice(result.device)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the recycler view with the adapter
        binding.recyclerScan.layoutManager = LinearLayoutManager(this)
        binding.recyclerScan.adapter = DeviceAdapter(ArrayList(), itemClickListener)

        // Start scanning when the "Scan" button is clicked
        binding.iconStatusScan.setOnClickListener {
            // Check permissions
            if (allPermissionsGranted()) {
                scanDevices()
            } else {
                requestPermissionLauncher.launch(getAllPermissions())
            }
        }
    }

    private fun getAllPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        }else{
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        Toast.makeText(this, "AR: getAllPermissionsLeave", Toast.LENGTH_SHORT).show()

    }
    private fun allPermissionsGranted(): Boolean {
        val allPermissions = getAllPermissions()
        return allPermissions.all{
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        Toast.makeText(this, "allPermssionsGrantedLeave", Toast.LENGTH_SHORT).show()

    }


    @SuppressLint("MissingPermission")
    private fun scanDevices() {
        if (allPermissionsGranted()) {
            Toast.makeText(this, "PERMS OK", Toast.LENGTH_SHORT).show()
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show()
            } else {
                if (!bluetoothAdapter.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                } else {
                    val pairedDevices = bluetoothAdapter.bondedDevices
                    if (pairedDevices.isNotEmpty()) {
                        for (device in pairedDevices) {
                            val deviceName = device.name
                            val deviceHardwareAddress = device.address // MAC address
                            // Do something with the device information
                        }
                    } else {
                        Toast.makeText(this, "No device found", Toast.LENGTH_SHORT).show()
                    }
                    val scanner = bluetoothAdapter.bluetoothLeScanner
                    scanner.startScan(object : ScanCallback() {
                        override fun onScanResult(callbackType: Int, result: ScanResult) {
                            super.onScanResult(callbackType, result)
                            val device = result.device
                            val deviceName = device.name
                            val deviceHardwareAddress = device.address //MAC address du device
                        }
                    })
                }
            }
        } else {
            requestPermissionLauncher.launch(getAllPermissions())
        }
    }


    private val itemClickListener = object : DeviceAdapter.OnItemClickListener {
        override fun onItemClick(device: BluetoothDevice) {

        }
    }
}
