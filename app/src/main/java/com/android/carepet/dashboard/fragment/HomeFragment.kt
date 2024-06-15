package com.android.carepet.dashboard.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.android.carepet.R
import com.android.carepet.view.disease.DiseaseActivity
import com.android.carepet.view.medicine.MedicineActivity
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
        val showDisease = view.findViewById<LinearLayout>(R.id.showDisease)
        val showMedicine = view.findViewById<LinearLayout>(R.id.showMedicine)

        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)

        scanSkin.setOnClickListener {
            fab?.performClick()
        }

        showDisease.setOnClickListener {
            val intent = Intent(activity, DiseaseActivity::class.java)
            startActivity(intent)
        }

        showMedicine.setOnClickListener {
            val intent = Intent(activity, MedicineActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}