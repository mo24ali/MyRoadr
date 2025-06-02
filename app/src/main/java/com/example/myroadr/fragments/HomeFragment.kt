package com.example.myroadr.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myroadr.Adpaters.CyclingEventAdapter
import com.example.myroadr.R
import com.example.myroadr.activities.AddEventActivity
import com.example.myroadr.databinding.FragmentHomeBinding
import com.example.myroadr.models.CyclingEvent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.OkHttpClient
import okhttp3.Request

import androidx.navigation.fragment.findNavController

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.util.Date
import java.util.Locale
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var adapter: CyclingEventAdapter

    private val apiKey = "6a3234657877bc9901fd3c4030bff8b0"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getLocationAndWeather()
        } else {
            Toast.makeText(requireContext(), "Permission localisation refusée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        checkLocationPermission()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dateFormat = SimpleDateFormat("dd MMMM, EEEE", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        binding.currentD.text = currentDate

        binding.fabAddEvent.setOnClickListener {
            val intent = Intent(requireContext(), AddEventActivity::class.java)
            startActivity(intent)
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId ?: "")
        dbRef.child("username").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java)
                binding.greetingsId.text = "Hello ${username ?: "User"},"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erreur chargement nom", Toast.LENGTH_SHORT).show()
            }
        })

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewEvents)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val userLocation = Location("me").apply {
            latitude = 33.6
            longitude = -7.5
        }

        adapter = CyclingEventAdapter(
            userLocation,
            onJoinClick = { event ->
                val context = requireContext()
                val currentUser = FirebaseAuth.getInstance().currentUser

                if (currentUser != null && event.id.isNotEmpty()) {
                    AlertDialog.Builder(context)
                        .setTitle("Confirmer")
                        .setMessage("Voulez-vous vraiment rejoindre l’événement « ${event.title} » ?")
                        .setPositiveButton("Oui") { dialog, _ ->
                            val uid = currentUser.uid

                            val joinedRef = FirebaseDatabase.getInstance()
                                .getReference("Events")
                                .child(event.id)
                                .child("joinedUsers")
                                .child(uid)

                            joinedRef.setValue(true)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Inscription réussie !", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
                                }

                            dialog.dismiss()
                        }
                        .setNegativeButton("Annuler") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                } else {
                    Toast.makeText(context, "Veuillez vous connecter", Toast.LENGTH_SHORT).show()
                }
            },

            onFavoriteClick = { event ->
                Toast.makeText(requireContext(), "Favori: ${event.title}", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.adapter = adapter

        // Load events from Firebase
        val eventsRef = FirebaseDatabase.getInstance().getReference("Events")
        eventsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<CyclingEvent>()
                for (child in snapshot.children) {
                    val event = child.getValue(CyclingEvent::class.java)
                    if (event != null) events.add(event)
                }
                adapter.updateData(events)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erreur chargement événements", Toast.LENGTH_SHORT).show()
            }
        })

        ////////////////////////////////////////

    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getLocationAndWeather()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(requireContext(), "Cette permission est nécessaire pour afficher la météo", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getLocationAndWeather() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                fetchWeather(location.latitude, location.longitude)
            } else {
                Toast.makeText(requireContext(), "Locaalisation introuvablee", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: "{}"
                val json = JSONObject(body)

                val weather = json.getJSONArray("weather").getJSONObject(0).getString("main")
                val temp = json.getJSONObject("main").getDouble("temp")
                val city = json.getString("name")

                withContext(Dispatchers.Main) {
                    binding.tempText.text = "${temp.toInt()}°  $weather"
                    binding.cityText.text = city
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Erreur lors de la récupération météo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
