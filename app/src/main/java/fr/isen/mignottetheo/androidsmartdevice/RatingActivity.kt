package fr.isen.mignottetheo.androidsmartdevice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RatingBar
import android.widget.Toast
import fr.isen.mignottetheo.androidsmartdevice.databinding.ActivityRatingBinding

class RatingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRatingBinding
    private lateinit var ratingBar: RatingBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ratingBar = binding.ratingBar
        ratingBar.onRatingBarChangeListener = object : RatingBar.OnRatingBarChangeListener {
            override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
                if (rating > 3) {
                    binding.reactionRating.setImageResource(R.drawable.grogu_happy)
                } else {
                    binding.reactionRating.setImageResource(R.drawable.grogu_sad)
                }
            }
        }
    }
}