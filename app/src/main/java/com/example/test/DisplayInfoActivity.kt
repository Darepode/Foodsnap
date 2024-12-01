package com.example.test

import android.R.drawable.ic_menu_gallery
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File

class DisplayInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display)

        // Get references to views
        val textViewInfo = findViewById<TextView>(R.id.textViewInfo)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val buttonBack = findViewById<Button>(R.id.buttonBack)
        val circularProgressView = findViewById<CircularProgressView>(R.id.circularProgressView)
        val textMessage = findViewById<TextView>(R.id.textMessage)
        val textFilename = findViewById<TextView>(R.id.textFilename)
        val textTimestamp = findViewById<TextView>(R.id.textTimestamp)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        // Get the server response passed via Intent
        val serverResponse = intent.getStringExtra("server_response") ?: "No response received"
        val imagePath = intent.getStringExtra("image_path") ?: ""

        // Extract the "Item" from the response using string operations
        val itemLine = serverResponse.lines().find { it.startsWith("Item:") } ?: "Item: None"
        val item = itemLine.substringAfter("Item:").trim()
        val messageLine = serverResponse.lines().find { it.startsWith("Message:") } ?: "Message: N/A"
        val message = messageLine.substringAfter("Message:").trim() // Extract only the value
        val filename = serverResponse.lines().find { it.startsWith("Filename:") } ?: "Filename: N/A"
        val timestamp = serverResponse.lines().find { it.startsWith("Timestamp:") } ?: "Timestamp: N/A"

        // Extract nutritional values and common allergens
        val nutritionalValues = serverResponse.lines()
            .find { it.startsWith("Description: ") }
            ?.substringAfter("Description: ")
            ?.split(",") // Split by comma
            ?.map { it.trim().replaceFirstChar { char -> char.uppercaseChar() } } // Trim and capitalize
            ?: listOf("No nutritional values available")

        val commonAllergens = serverResponse.lines()
            .find { it.startsWith("Common Allergens: ") }
            ?.substringAfter("Common Allergens: ")
            ?.split(",") // Split by comma
            ?.map { it.trim().replaceFirstChar { char -> char.uppercaseChar() } } // Trim and capitalize
            ?: listOf("No common allergens available")

        // Reference to the "Cook it" box
        val cookItBox = findViewById<LinearLayout>(R.id.cookItBox)

        // Extract main ingredients from server response
        val mainIngredientLine = serverResponse.lines().find { it.startsWith("Main Ingredients:") } ?: "Main Ingredients: N/A"
        val mainIngredients = mainIngredientLine.substringAfter("Main Ingredients:").trim()
            .split(",") // Split by commas to handle multiple ingredients
            .map { it.trim() } // Trim spaces around each ingredient

        fun showCookItDialog() {
            // Create a dialog with your custom layout
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_cook_it)

            dialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.92).toInt(), // 85% of screen width
                ViewGroup.LayoutParams.WRAP_CONTENT // Adjust height dynamically based on content
            )

            // Make the dialog background transparent for rounded corners
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Close button functionality
            val closeDialog = dialog.findViewById<ImageView>(R.id.closeDialog)
            closeDialog.setOnClickListener {
                dialog.dismiss()
            }

            // Set up the ingredients in two columns with bullet points
            val ingredientsContainer = dialog.findViewById<GridLayout>(R.id.ingredientsContainer)
            ingredientsContainer.apply {
                rowCount = (mainIngredients.size + 1) / 2 // Calculate rows dynamically
                columnCount = 2
            }

            // Margins for columns
            val firstColumnMargins = Pair(70, 8) // (left, right margins for column 1)
            val secondColumnMargins = Pair(120, 0) // (left, right margins for column 2)

            mainIngredients.forEachIndexed { index, ingredient ->
                // Create a TextView for each ingredient
                val ingredientTextView = TextView(this).apply {
                    text = "• $ingredient" // Add bullet point
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(this@DisplayInfoActivity, android.R.color.white))
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0 // Equal width for columns
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(index % 2, 1f) // Specify column
                        gravity = Gravity.START // Align text to the start

                        // Adjust margins based on the column
                        if (index % 2 == 0) { // First column
                            setMargins(firstColumnMargins.first, 8, firstColumnMargins.second, 8)
                        } else { // Second column
                            setMargins(secondColumnMargins.first, 8, secondColumnMargins.second, 8)
                        }
                    }
                }

                ingredientsContainer.addView(ingredientTextView)
            }

            // Set the video dynamically
            val youtubeVideo = dialog.findViewById<WebView>(R.id.youtubeVideo)
            youtubeVideo.settings.javaScriptEnabled = true

            // Define a map of food names to YouTube video IDs
            val videoUrls = mapOf(
                "Apple Pie" to "KbyahTnzbKA",
                "Chicken Wings" to "nbIwmixBIxk",
                "Donuts" to "aFaQ2cxYxbM",
                "Fried Rice" to "LU_r-NWdfSk",
                "Hot Dog" to "y2Ku3nWx1XI",
                "Omelette" to "ixpYIgHlU60",
                "Pho" to "9Sg21AHuO4A",
                "Steak" to "AmC9SmCBUj4",
                "Sushi" to "nIoOv6lWYnk",
                "Takoyaki" to "-d8nTfzxES8",
                "Hamburger" to "BIG1h2vG-Qg",
                "Macarons" to "MjVgIXccYXA",
                "Nachos" to "5RoWxdsF5Wc",
                "Spring Rolls" to "GMw9py4Y5is",
                "Pizza" to "sv3TXMSv6Lw",
                "Peking Duck" to "KnJ3abXjgME",
                "Greek Salad" to "kwq4vl610iY",
                "Cup Cakes" to "uU7eOlSL6Hk",
                "Tiramisu" to "87V4nizNJiE",
                "Frozen Yogurt" to "rzXkiFZM1Vc"
            )


            // Get the video ID for the predicted food
            val videoId = videoUrls[item] ?: "default_video_id" // Fallback to a default video

            // Load the video in the WebView
            youtubeVideo.loadData(
                """
            <html>
                <body style="margin:0;padding:0;">
                    <iframe width="100%" height="100%" src="https://www.youtube.com/embed/$videoId" 
                    frameborder="0" allowfullscreen></iframe>
                </body>
            </html>
        """.trimIndent(),
                "text/html",
                "utf-8"
            )

            // Show the dialog
            dialog.show()
        }

        // Set click listener for the "Cook it" box
        cookItBox.setOnClickListener {
            showCookItDialog()
        }

        // Set the "Item" text in the TextView
        textViewInfo.text = item

        // Display the image if the path is valid
        if (imagePath.isNotEmpty()) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                imageView.setImageURI(Uri.fromFile(imageFile))
            } else {
                imageView.setImageResource(ic_menu_gallery) // Default gallery icon
            }
        }

        // Set the message, filename, and timestamp
        textMessage.text = message
        textFilename.text = filename
        textTimestamp.text = timestamp

        // Extract the confidence percentage and set the progress in CircularProgressView
        val confidenceLine = serverResponse.lines().find { it.startsWith("Confidence:") } ?: "Confidence: 0"
        val confidence = confidenceLine.substringAfter("Confidence:").trim().toFloatOrNull() ?: 0f
        circularProgressView.setProgress(confidence * 100)

        // Set up ViewPager2 adapter
        val adapter = TabsPagerAdapter(this, nutritionalValues, commonAllergens)
        viewPager.adapter = adapter

        // Link TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Nutritional Values" else "Common Allergens"
        }.attach()


        // Handle Back Button click
        buttonBack.setOnClickListener {
            // Navigate back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Optional: Finish this activity
        }
    }
}
