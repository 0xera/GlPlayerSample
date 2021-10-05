package com.uzicus.glplayersample.utils

import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.databinding.BindingAdapter

interface OnSpinnerSelectedPositionListener {
    fun onSelectedPosition(position: Int)
}

@BindingAdapter(value = ["selectedItemPosition"])
fun bindSpinnerSelectPositionListener(spinner: AppCompatSpinner, listener: OnSpinnerSelectedPositionListener) {
    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            listener.onSelectedPosition(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }
    }
}