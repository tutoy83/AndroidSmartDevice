package fr.isen.mignottetheo.androidsmartdevice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import fr.isen.mignottetheo.androidsmartdevice.databinding.ActivityDeviceDetailsBinding

class DeviceDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceDetailsBinding
    private var led1Active = false
    private var led2Active = false
    private var led3Active = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityDeviceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleDeviceDetails.text= intent.getStringExtra("deviceName")

        binding.led1Icon.setOnClickListener {            //Manage click on Led2
            if(led1Active==false){
                //Led2 was INACTIVE
                binding.led1Icon.setImageResource(R.drawable.led_on)
                led1Active = true
            }else{
                //Led2 was ACTIVE
                binding.led1Icon.setImageResource(R.drawable.led_off)
                led1Active = false
            }
        }

        binding.led2Icon.setOnClickListener {            //Manage click on Led2
            if(led2Active==false){
                //Led2 was INACTIVE
                binding.led2Icon.setImageResource(R.drawable.led_on)
                led2Active = true
            }else{
                //Led2 was ACTIVE
                binding.led2Icon.setImageResource(R.drawable.led_off)
                led2Active = false
            }
        }

        binding.led3Icon.setOnClickListener {            //Manage click on Led2
            if(led3Active==false){
                //Led3 was INACTIVE
                binding.led3Icon.setImageResource(R.drawable.led_on)
                led3Active = true
            }else{
                //Led3 was ACTIVE
                binding.led3Icon.setImageResource(R.drawable.led_off)
                led3Active = false
            }
        }
        binding.endDeviceButton.setOnClickListener {
            val intent = Intent(this, RatingActivity::class.java)
            startActivity(intent)
        }
    }


}
