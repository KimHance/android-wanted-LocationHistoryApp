package com.preonboarding.locationhistory.feature.set

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.preonboarding.locationhistory.LocationHistoryApp
import com.preonboarding.locationhistory.R
import com.preonboarding.locationhistory.databinding.DialogSetTimeBinding

class SetTimeDialog : DialogFragment() {
    private lateinit var binding: DialogSetTimeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DialogSetTimeBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        binding.apply {
            btnSetTimeCancel.setOnClickListener {
                dismiss()
            }
            btnSetTimePositive.setOnClickListener {
                if (editSetText.text.toString() == "" || editSetText.text.toString() == "0") {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.set_time_dialog_warning),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    LocationHistoryApp.prefs.setTime =
                        (editSetText.text.toString().toInt() * 60000L).toString()
                    dismiss()
                }
            }
        }
    }
}