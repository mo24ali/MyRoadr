package com.example.myroadr


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myroadr.databinding.ActivityMainBinding
import com.example.myroadr.Fragments.EventDiscoveryFragment
import com.example.myroadr.Fragments.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(HomeFragment())

//        binding.bottomNav.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.nav_home -> replaceFragment(HomeFragment())
//                R.id.nav_grid -> replaceFragment(EventDiscoveryFragment())
//            }
//            true
//        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}