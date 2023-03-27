package fr.isen.mignottetheo.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
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
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.*

import fr.isen.mignottetheo.androidsmartdevice.databinding.ActivityScanBinding



class ScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanBinding
    private var active = 0
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val mHandler = Handler(Looper.getMainLooper()) // Specify Looper for Handler
    private val REQUEST_PERMISSION_BLUETOOTH_CONNECT = 1
    private var mLeDeviceListAdapter: DeviceAdapter? = null
    private val mLeDevices = ArrayList<BluetoothDevice>()

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (!allPermissionsGranted()) {
                requestAllPermissions()
            }
        }

        // Use this check to determine whether BLE is supported on the device.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Initializes a Bluetooth adapter.
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }


    @SuppressLint("MissingPermission")
    private fun scanLeDevice(enable: Boolean) {
        if (allPermissionsGranted()) {
            if (enable) {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed({
                    active = 0
                    mBluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
                    invalidateOptionsMenu()
                }, SCAN_PERIOD)
                active = 1
                mBluetoothAdapter?.bluetoothLeScanner?.startScan(mLeScanCallback)
            } else {
                active = 0
                mBluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
            }
            invalidateOptionsMenu()
        } else {
            requestAllPermissions()
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    baseContext, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun requestAllPermissions() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            REQUEST_PERMISSION_BLUETOOTH_CONNECT
        )
    }

    private val itemClickListener = object : DeviceAdapter.OnItemClickListener {
        override fun onItemClick(device: BluetoothDevice) {

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_BLUETOOTH_CONNECT -> {
                if (allPermissionsGranted()) {
                } else {
                    Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
