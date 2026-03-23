package com.cherninlab.motionrelief

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ReliefActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ReliefView(this))
    }
}
