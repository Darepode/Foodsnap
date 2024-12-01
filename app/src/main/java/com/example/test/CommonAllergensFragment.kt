package com.example.test

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class CommonAllergensFragment : Fragment() {
    private lateinit var content: List<String> // List of allergens

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val rawContent = it.getString("content", "No common allergens available")
            content = rawContent.split("\n").map { it.trim() }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Create a GridLayout to display allergens in 2 columns
        val gridLayout = GridLayout(requireContext()).apply {
            rowCount = (content.size + 1) / 2 // Calculate the number of rows
            columnCount = 2
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 16)
        }

        // Colors for allergens (customized for white text visibility and contrast with #313747 background)
        val colors = listOf(
            R.color.shade_highlight,
            R.color.shade_cool_gray,
            R.color.shade_lighter,
            R.color.shade_muted,
            R.color.shade_warm_gray,
            R.color.shade_darker
        )

        // Add allergens to the GridLayout
        content.forEachIndexed { index, allergen ->
            val allergenBox = TextView(requireContext()).apply {
                text = allergen
                textSize = 16f
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                gravity = Gravity.CENTER
                setPadding(16, 16, 16, 16)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_box) // Use the rounded drawable
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // Equal column width
                    setMargins(8, 8, 8, 8)
                }
            }

            // Add background color from the list of colors to the rounded box
            allergenBox.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(),
                colors[index % colors.size]
            )

            gridLayout.addView(allergenBox)
        }

        return gridLayout
    }

    companion object {
        fun newInstance(content: String) = CommonAllergensFragment().apply {
            arguments = Bundle().apply {
                putString("content", content)
            }
        }
    }
}
