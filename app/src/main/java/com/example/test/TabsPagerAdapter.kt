package com.example.test

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

open class TabsPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val nutritionalValues: List<String>,
    private val commonAllergens: List<String>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NutritionalValuesFragment.newInstance(nutritionalValues.joinToString(separator = "\n"))
            1 -> CommonAllergensFragment.newInstance(commonAllergens.joinToString(separator = "\n"))
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}

