package com.example.myroadr.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myroadr.databinding.FragmentEventDiscoveryBinding

class EventDiscoveryFragment : Fragment() {

    private var _binding: FragmentEventDiscoveryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDiscoveryBinding.inflate(inflater, container, false)
        binding.recyclerEvents.layoutManager = LinearLayoutManager(context)
        // TODO: set adapter with real data
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
