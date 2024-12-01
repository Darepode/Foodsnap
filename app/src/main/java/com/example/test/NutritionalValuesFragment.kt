package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class NutritionalValuesFragment : Fragment() {
    private lateinit var content: List<Pair<String, String>> // List of (Name, Value)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val rawContent = it.getString("content", "No nutritional values available")
            content = rawContent.split("\n").map {
                val parts = it.split(":").map { part -> part.trim() }
                if (parts.size == 2) parts[0] to parts[1] else "Unknown" to "Unknown"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        content.forEachIndexed { index, pair ->
            // Row Layout
            val rowLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(48, 8, 48, 8) // Align rows with divider lines
                }
            }

            // Left TextView (Name of nutrition)
            val leftTextView = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f // Allocate space proportionally
                )
                text = pair.first
                textSize = 16f
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }

            // Right TextView (Value of nutrition)
            val rightTextView = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f // Allocate space proportionally
                )
                text = pair.second
                textSize = 16f
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            }

            // Divider Line
            val divider = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                ).apply {
                    setMargins(48, 8, 48, 8) // Ensure divider aligns with text rows
                }
                setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            }

            // Add TextViews to the row layout
            rowLayout.addView(leftTextView)
            rowLayout.addView(rightTextView)

            // Add row layout and divider to the parent layout
            layout.addView(rowLayout)
            if (index != content.lastIndex) {
                layout.addView(divider) // Add divider only between rows
            }
        }

        return layout
    }

    companion object {
        fun newInstance(content: String) = NutritionalValuesFragment().apply {
            arguments = Bundle().apply {
                putString("content", content)
            }
        }
    }
}
