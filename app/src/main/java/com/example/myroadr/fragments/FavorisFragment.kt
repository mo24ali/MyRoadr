package com.example.myroadr.fragments

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myroadr.Adpaters.CyclingEventAdapter
import com.example.myroadr.databinding.FragmentFavorisBinding

import com.example.myroadr.util.FavoritesManager


class FavorisFragment : Fragment() {

    private var _binding : FragmentFavorisBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CyclingEventAdapter
    private lateinit var userLocation: Location

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavorisBinding.inflate(inflater, container, false)

        userLocation = Location("user").apply {
            latitude = 33.6
            longitude = -7.5
        }

        FavoritesManager.loadFromFirebase {
            setupRecyclerView()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        val favoris = FavoritesManager.getAll()

        adapter = CyclingEventAdapter(
            userLocation,
            onJoinClick = { /* action si besoin */ },
            onFavoriteClick = {
                adapter.updateData(FavoritesManager.getAll())
                updateEmptyState()
            }
        )

        binding.recyclerViewFavoris.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavoris.adapter = adapter
        adapter.updateData(favoris)

        updateEmptyState()
    }

    private fun updateEmptyState() {
        val hasFavoris = FavoritesManager.getAll().isNotEmpty()
        binding.recyclerViewFavoris.visibility = if (hasFavoris) View.VISIBLE else View.GONE
        binding.emptyStateLayout.visibility = if (hasFavoris) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        FavoritesManager.loadFromFirebase {
            if (::adapter.isInitialized) {
                adapter.updateData(FavoritesManager.getAll())
                updateEmptyState()
            } else {
                setupRecyclerView()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
