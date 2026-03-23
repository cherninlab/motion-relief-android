package com.cherninlab.motionrelief

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cherninlab.motionrelief.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        renderState()

        binding.primaryActionButton.setOnClickListener {
            isEnabled = !isEnabled
            renderState()
        }
    }

    private fun renderState() {
        binding.stateValue.text = if (isEnabled) getString(R.string.state_on) else getString(R.string.state_off)
        binding.primaryActionButton.text = if (isEnabled) getString(R.string.stop) else getString(R.string.start)
        binding.supportingText.text = if (isEnabled) {
            getString(R.string.enabled_supporting_text)
        } else {
            getString(R.string.disabled_supporting_text)
        }
    }
}
