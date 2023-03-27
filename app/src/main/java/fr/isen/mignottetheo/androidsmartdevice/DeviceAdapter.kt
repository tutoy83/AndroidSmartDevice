package fr.isen.mignottetheo.androidsmartdevice

import android.Manifest
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
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView


class DeviceAdapter(private val devices: MutableList<BluetoothDevice>, private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

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
                val device = devices[position]
                itemClickListener.onItemClick(device)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_cell, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        if (ActivityCompat.checkSelfPermission(
                holder.itemView.context, // Fix the context issue here
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        holder.deviceName.text = device.name ?: "Unnamed Device"
        holder.deviceAddress.text = device.address
        holder.signalValue.text = "-41"
    }

    override fun getItemCount() = devices.size

    fun addDevice(device: BluetoothDevice) {
        if (!devices.contains(device)) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }

    fun clearDevices() {
        devices.clear()
        notifyDataSetChanged()
    }
}

