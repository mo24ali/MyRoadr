package com.example.myroadr.activities

import MapsFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.myroadr.fragments.FavorisFragment
import com.example.myroadr.fragments.HomeFragment
import com.example.myroadr.fragments.*
import com.example.myroadr.fragments.ProfileFragment
import com.example.myroadr.R
import me.ibrahimsn.lib.SmoothBottomBar

class MainActivity : AppCompatActivity() {
    private lateinit var bottomBar: SmoothBottomBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        bottomBar = findViewById(R.id.bottomBar)

        // Load default fragment
        loadFragment(HomeFragment())

        // Set listener
        bottomBar.setOnItemSelectedListener { index ->
            val fragment: Fragment = when (index) {
                0 -> HomeFragment()
                1 -> MapsFragment()
                2 -> FavorisFragment()
                3 -> ProfileFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, fragment)
            .commit()
    }
}