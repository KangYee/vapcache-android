package io.github.kangyee.vapcache.ui

import android.app.Activity
import android.os.Bundle
import io.github.kangyee.vapcache.databinding.ActivityExampleBinding

class ExampleActivity : Activity() {

    private lateinit var mBinding: ActivityExampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityExampleBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initResRaw()
    }

    private fun initResRaw() {
        mBinding.avRawres.startPlay()
    }

}