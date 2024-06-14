package com.android.carepet.dashboard.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.android.carepet.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val scanSkin = view.findViewById<LinearLayout>(R.id.scanSkin)

        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)

        scanSkin.setOnClickListener {
            fab?.performClick()
        }

        return view
    }
}
