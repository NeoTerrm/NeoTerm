package io.neomodule.testapp

import android.app.Activity
import android.os.Bundle
import io.neomodule.layout.NeoLayoutInflater
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        load.setOnClickListener {
            val view = NeoLayoutInflater.inflate(this, assets.open("dynamic_layout.xml"))
            setContentView(view)
        }
    }
}
