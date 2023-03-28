package fr.isen.mignottetheo.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
    private val mLeDevices = ArrayList<BluetoothDevice>()

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            //Callback when permissions OK
            if (permissions.all { it.value }) {
                scanDevices()
            }
        }

    fun addDevice(device: BluetoothDevice) {
        if (mLeDevices.isEmpty()) {
            mLeDevices.add(device)

        } else {
            // Check if the device already in the adapter
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device)
            }
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        lateinit var bluetoothGatt: BluetoothGatt
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
    }


    @SuppressLint("MissingPermission")
    private fun scanDevices() {
        if (allPermissionsGranted()) {
            val bluetoothAdapter = getBluetoothAdapter()
            if (bluetoothAdapter == null) {
                showBluetoothNotAvailableError()
            } else {
                if (!isBluetoothEnabled(bluetoothAdapter)) {
                    requestBluetoothEnable(bluetoothAdapter)
                } else {
                    startBluetoothScan(bluetoothAdapter)
                }
            }
        } else {
            requestPermissions()
        }
    }

    private fun getBluetoothAdapter(): BluetoothAdapter? {
        return BluetoothAdapter.getDefaultAdapter()
    }

    private fun showBluetoothNotAvailableError() {
        Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show()
    }

    private fun isBluetoothEnabled(bluetoothAdapter: BluetoothAdapter): Boolean {
        return bluetoothAdapter.isEnabled
    }
    @SuppressLint("MissingPermission")
    private fun requestBluetoothEnable(bluetoothAdapter: BluetoothAdapter) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    @SuppressLint("MissingPermission")
    private fun startBluetoothScan(bluetoothAdapter: BluetoothAdapter) {
        //Change layout (progress bar, button etc)
        binding.iconStatusScan.setImageResource(R.drawable.baseline_pause_circle_24)
        binding.progressBarScan.isIndeterminate = true
        binding.titleScan.text = "Scan en cours..."
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        scanner?.startScan(getScanCallback())
        Handler().postDelayed({
            if (scanner != null) {
                stopBluetoothScan(scanner)
            }
        }, 12000) //12 seconds
    }

    @SuppressLint("MissingPermission")
    private fun getScanCallback(): ScanCallback {
        return object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val device = result.device
                if (!device.name.isNullOrEmpty()) {
                    //remove all devices without names that are polluting
                    (binding.recyclerScan.adapter as DeviceAdapter).addDevice(device)
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun stopBluetoothScan(scanner: BluetoothLeScanner) {
        //Important: stop Bluetooth to save battery
        scanner.stopScan(getScanCallback())
        binding.iconStatusScan.setImageResource(R.drawable.baseline_play_circle_24)
        binding.progressBarScan.isIndeterminate = false
        binding.titleScan.text = "Relancer un scan (12s) "

    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(getAllPermissions())
    }

    @SuppressLint("MissingPermission")
    private val itemClickListener = object : DeviceAdapter.OnItemClickListener {
        override fun onItemClick(device: BluetoothDevice) {
            Toast.makeText(this@ScanActivity, "Connecting to : ${device.name}, please wait.", Toast.LENGTH_SHORT).show()
            bluetoothConnectionToDevice(device)
        }
    }

    @SuppressLint("MissingPermission")
    private fun bluetoothConnectionToDevice(device: BluetoothDevice) {
        device.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    val intent = Intent(this@ScanActivity, DeviceDetailsActivity::class.java)
                    intent.putExtra("deviceName", device.name)
                    intent.putExtra("deviceAddress", device.address)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@ScanActivity, "Connection failed !!", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }




}
