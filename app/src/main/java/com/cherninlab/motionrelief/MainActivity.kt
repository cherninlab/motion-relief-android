package com.cherninlab.motionrelief

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cherninlab.motionrelief.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.stateValue.text = getString(R.string.state_ready)
        binding.primaryActionButton.text = getString(R.string.start_relief_mode)
        binding.supportingText.text = getString(R.string.home_supporting_text)

        binding.primaryActionButton.setOnClickListener {
            startActivity(Intent(this, ReliefActivity::class.java))
        }
    }
}
