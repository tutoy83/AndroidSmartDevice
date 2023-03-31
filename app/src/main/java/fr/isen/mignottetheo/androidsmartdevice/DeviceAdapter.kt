package fr.isen.mignottetheo.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeviceAdapter(
    private val mLeDevices: ArrayList<BluetoothDevice>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    private val mRssiValues = HashMap<String, Int>() // New member variable to store RSSI values
    // Inner interface for item click listener
    interface OnItemClickListener {
        fun onItemClick(device: BluetoothDevice)
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val deviceName: TextView = itemView.findViewById(R.id.cellName)
        val deviceAddress: TextView = itemView.findViewById(R.id.cellMacAdress)
        val signalValue: TextView = itemView.findViewById(R.id.cellSignal)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val device = mLeDevices[position]
                itemClickListener.onItemClick(device)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_cell, parent, false)
        return DeviceViewHolder(view)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = mLeDevices[position]
        holder.deviceName.text = device.name ?: "Unnamed Device"
        holder.deviceAddress.text = device.address

        // Set the RSSI value
        val rssi = mRssiValues[device.address]
        holder.signalValue.text = rssi?.toString() ?: "N/A"
    }


    override fun getItemCount() = mLeDevices.size

    fun addDevice(device: BluetoothDevice, rssi: Int) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device)
            mRssiValues[device.address] = rssi // Store the RSSI value
            notifyDataSetChanged()
        }
    }

    fun clearDevices() {
        mLeDevices.clear()
        notifyDataSetChanged()
    }
}


