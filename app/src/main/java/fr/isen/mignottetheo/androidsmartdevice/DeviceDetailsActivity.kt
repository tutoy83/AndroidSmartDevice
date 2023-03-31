package fr.isen.mignottetheo.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import fr.isen.mignottetheo.androidsmartdevice.databinding.ActivityDeviceDetailsBinding
import java.util.*

class DeviceDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceDetailsBinding
    private var ledCharacteristic: BluetoothGattCharacteristic? = null
    private var buttonCharacteristic: BluetoothGattCharacteristic? = null
    private lateinit var bluetoothGatt: BluetoothGatt
    private var led1Active = false
    private var led2Active = false
    private var led3Active = false


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.titleDeviceDetails.text = intent.getStringExtra("deviceName")
        bluetoothGatt = BluetoothAdapter.getDefaultAdapter()
            .getRemoteDevice(intent.getStringExtra("deviceAddress"))
            .connectGatt(this, false, gattCallback)


        binding.led1Icon.setOnClickListener {
            led1Management()
        }
        binding.led2Icon.setOnClickListener {
            led2Management()
        }
        binding.led3Icon.setOnClickListener {
            led3Management()
        }
        binding.endDeviceButton.setOnClickListener {
            // Stop listening, bluetooth deconnexion
            bluetoothGatt.setCharacteristicNotification(ledCharacteristic, false)
            bluetoothGatt.disconnect()
            bluetoothGatt.close()
            val intent = Intent(this, RatingActivity::class.java)
            startActivity(intent)

        }

        binding.optionNotifDeviceDetails.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this@DeviceDetailsActivity, "Come back later :)", Toast.LENGTH_SHORT).show()
            if (ledCharacteristic != null) {
                enableNotification(isChecked)

            }
        }

    }

    @SuppressLint("MissingPermission")
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt?.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //UUID checked on nRF Connect IOT app
                val service =
                    gatt?.getService(UUID.fromString("0000FEED-CC7A-482A-984A-7F2ED5B3E58F"))
                ledCharacteristic =
                    service?.getCharacteristic(UUID.fromString("0000ABCD-8E22-4541-9D4C-21EDAE82ED19"))

                //Get the descriptor for characteristic and enable notifications
                val descriptor: BluetoothGattDescriptor? =
                    ledCharacteristic?.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                bluetoothGatt.writeDescriptor(descriptor)

                //Register for onCharacteristicChanged callback
                bluetoothGatt.setCharacteristicNotification(ledCharacteristic, true)



                //nRF Connect shows: it is the same service UUID but different buttonCharacteristic UUID

                buttonCharacteristic =
                    service?.getCharacteristic(UUID.fromString("00001234-8E22-4541-9D4C-21EDAE82ED19"))
                //Get the descriptor for characteristic and enable notifications
                val descriptor2: BluetoothGattDescriptor? =
                    buttonCharacteristic?.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor2?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                bluetoothGatt.writeDescriptor(descriptor2)

                //Register onCharacteristicChanged callback
                bluetoothGatt.setCharacteristicNotification(buttonCharacteristic, true)

            }
        }


    }

    @SuppressLint("MissingPermission")
    private fun enableNotification(enable: Boolean) {
        if (bluetoothGatt == null || ledCharacteristic == null) {
            return
        }
        //UUID checked on nRF Connect IOT app
        val descriptor: BluetoothGattDescriptor? = ledCharacteristic!!.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
        if (descriptor == null) {
            return
        }
        bluetoothGatt.setCharacteristicNotification(ledCharacteristic, enable )
        descriptor.value =
            if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        bluetoothGatt.writeDescriptor(descriptor)

        bluetoothGatt.setCharacteristicNotification(ledCharacteristic, true)
        bluetoothGatt.readCharacteristic(ledCharacteristic)


        // Call onCharacteristicChanged function
        val characteristic: BluetoothGattCharacteristic? = ledCharacteristic
        if (characteristic != null) {
            onCharacteristicChanged(bluetoothGatt, characteristic)
        }
    }

    fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        runOnUiThread {
            Log.d("COMPTEUR", "NOTIF RECUE")

            if (characteristic?.uuid == UUID.fromString("00001234-8E22-4541-9D4C-21EDAE82ED19")) {
                val hexValue = characteristic?.getStringValue(0)
                Log.d("COMPTEUR", "NOTIF RECUE / Hex value = $hexValue")
            }
        }
    }
    @SuppressLint("MissingPermission")
    fun led1Management() {
        if (led1Active == false) {
            //Led1 was INACTIVE
            binding.led1Icon.setImageResource(R.drawable.led_on)
            binding.led2Icon.setImageResource(R.drawable.led_off)
            binding.led3Icon.setImageResource(R.drawable.led_off)
            ledCharacteristic?.setValue(byteArrayOf(0x01))
            bluetoothGatt.writeCharacteristic(ledCharacteristic)
            led1Active = true
            led2Active = false
            led3Active = false
        } else {
            //Led1 was ACTIVE
            binding.led1Icon.setImageResource(R.drawable.led_off)
            binding.led2Icon.setImageResource(R.drawable.led_off)
            binding.led3Icon.setImageResource(R.drawable.led_off)
            ledCharacteristic?.setValue(byteArrayOf(0x00))
            bluetoothGatt.writeCharacteristic(ledCharacteristic)
            led1Active = false
            led2Active = true
            led3Active = false
        }
    }

    @SuppressLint("MissingPermission")
    fun led2Management() {
        if (led2Active == false) {
            //Led2 was INACTIVE
            binding.led1Icon.setImageResource(R.drawable.led_off)
            binding.led2Icon.setImageResource(R.drawable.led_on)
            binding.led3Icon.setImageResource(R.drawable.led_off)
            ledCharacteristic?.setValue(byteArrayOf(0x02))
            bluetoothGatt.writeCharacteristic(ledCharacteristic)
            led1Active = false
            led2Active = true
            led3Active = false

        } else {
            //Led2 was ACTIVE
            binding.led1Icon.setImageResource(R.drawable.led_off)
            binding.led2Icon.setImageResource(R.drawable.led_off)
            binding.led3Icon.setImageResource(R.drawable.led_off)
            ledCharacteristic?.setValue(byteArrayOf(0x00))
            bluetoothGatt.writeCharacteristic(ledCharacteristic)
            led1Active = false
            led2Active = false
            led3Active = false
        }
    }

    @SuppressLint("MissingPermission")
    fun led3Management() {
        if (led3Active == false) {
            //Led3 was INACTIVE
            binding.led1Icon.setImageResource(R.drawable.led_off)
            binding.led2Icon.setImageResource(R.drawable.led_off)
            binding.led3Icon.setImageResource(R.drawable.led_on)
            ledCharacteristic?.setValue(byteArrayOf(0x03))
            bluetoothGatt.writeCharacteristic(ledCharacteristic)
            led1Active = false
            led2Active = false
            led3Active = true
        } else {
            //Led3 was ACTIVE
            binding.led1Icon.setImageResource(R.drawable.led_off)
            binding.led2Icon.setImageResource(R.drawable.led_off)
            binding.led3Icon.setImageResource(R.drawable.led_off)
            ledCharacteristic?.setValue(byteArrayOf(0x00))
            bluetoothGatt.writeCharacteristic(ledCharacteristic)
            led1Active = false
            led2Active = true
            led3Active = false
        }
    }
}



