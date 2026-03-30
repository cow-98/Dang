package com.android.dang

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.dang.databinding.ActivityMainBinding
import com.android.dang.detailFragment.DogDetailFragment
import com.android.dang.dictionary.DictionaryFragment
import com.android.dang.home.HomeFragment
import com.android.dang.like.LikeFragment
import com.android.dang.search.SearchFragment
import com.android.dang.search.searchItemModel.SearchDogData
import com.android.dang.shelter.view.ShelterFragment

class MainActivity : AppCompatActivity(), SearchFragment.DogData, HomeFragment.DogData {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val dogDetailFragment = DogDetailFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val homeFragment = HomeFragment()
        val searchFragment = SearchFragment()
        val shelterFragment = ShelterFragment()
        val likeFragment = LikeFragment()
        val dictionaryFragment = DictionaryFragment()

        binding.navBar.setItemActiveIndicatorEnabled(false)
        binding.navBar.itemActiveIndicatorColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
        binding.navBar.itemRippleColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
        binding.navBar.itemBackground = null

        switchFragment(homeFragment)
        binding.icBack.visibility = View.INVISIBLE

        binding.navBar.setOnItemSelectedListener {
            val activeFragment = supportFragmentManager.findFragmentById(binding.fragmentView.id)

            when (it.itemId) {
                R.id.menu_home -> if (activeFragment is HomeFragment) return@setOnItemSelectedListener true
                R.id.menu_shelter -> if (activeFragment is ShelterFragment) return@setOnItemSelectedListener true
                R.id.menu_like -> if (activeFragment is LikeFragment) return@setOnItemSelectedListener true
                R.id.menu_dictionary -> if (activeFragment is DictionaryFragment) return@setOnItemSelectedListener true
            }

            when (it.itemId) {
                R.id.menu_home -> {
                    binding.txtTitle.text = "Dang"
                    switchFragment(homeFragment)
                }

                R.id.menu_shelter -> {
                    binding.txtTitle.text = "댕지킴이"
                    switchFragment(shelterFragment)
                }

                R.id.menu_like -> {
                    binding.txtTitle.text = "댕찜"
                    switchFragment(likeFragment)
                }

                R.id.menu_dictionary -> {
                    binding.txtTitle.text = "댕댕백과"
                    switchFragment(dictionaryFragment)
                }
            }
            true
        }

        binding.icSearch.setOnClickListener {
            binding.txtTitle.text = "댕찾기"
            switchFragment(searchFragment)
        }

        binding.icBack.setOnClickListener {}

        homeFragment.dogData(this)
        searchFragment.dogData(this)
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentView.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_view, fragment)
            .setReorderingAllowed(true)
            .addToBackStack(null)
            .commit()
    }

    override fun pass(list: SearchDogData) {
        dogDetailFragment.receiveData(list)
        setFragment(dogDetailFragment)
    }

    override fun onBackPressed() {
        val activeFragment = supportFragmentManager.findFragmentById(binding.fragmentView.id)
        if (activeFragment is SearchFragment && activeFragment.handleSystemBackPressed()) {
            return
        }

        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
            binding.fragmentView.post {
                when (supportFragmentManager.findFragmentById(binding.fragmentView.id)) {
                    is HomeFragment -> binding.navBar.selectedItemId = R.id.menu_home
                    is ShelterFragment -> binding.navBar.selectedItemId = R.id.menu_shelter
                    is LikeFragment -> binding.navBar.selectedItemId = R.id.menu_like
                    is DictionaryFragment -> binding.navBar.selectedItemId = R.id.menu_dictionary
                }
            }
            return
        }

        finish()
    }
}
