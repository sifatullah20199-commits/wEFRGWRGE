
package com.sifatullah.votersearch

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private lateinit var db: VoterDatabase
    private val teal by lazy { ContextCompat.getColor(this, R.color.primary) }
    private val bg by lazy { ContextCompat.getColor(this, R.color.background) }
    private val textPrimary by lazy { ContextCompat.getColor(this, R.color.text_primary) }
    private val textSecondary by lazy { ContextCompat.getColor(this, R.color.text_secondary) }

    private val csvPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importCsv(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = VoterDatabase(this)
        if (db.count() == 0) db.importAsset("default_voters.csv")
        showHome()
    }

    private fun baseScroll(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(14), dp(20), dp(18))
            setBackgroundColor(bg)
        }
        return root
    }

    private fun tv(text: String, size: Float, color: Int = textPrimary, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
            gravity = Gravity.CENTER
        }

    private fun topBrand(root: LinearLayout) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        row.addView(tv("নারায়ে তাকবির", 13f, textPrimary, true), LinearLayout.LayoutParams(0, dp(36), 1f))
        row.addView(tv("بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ", 17f, textPrimary, false), LinearLayout.LayoutParams(0, dp(42), 1f))
        row.addView(tv("আল্লাহু আকবার", 13f, textPrimary, true), LinearLayout.LayoutParams(0, dp(36), 1f))
        root.addView(row)
    }

    private fun title(root: LinearLayout) {
        root.addView(tv("ভোটার তালিকা অনুসন্ধান", 27f, textPrimary, true).apply {
            setPadding(0, dp(14), 0, dp(18))
        })
    }

    private fun actionCard(label: String, subtitle: String, onClick: () -> Unit): View {
        val card = MaterialCardView(this).apply {
            radius = dp(18).toFloat()
            cardElevation = dp(1).toFloat()
            strokeWidth = 1
            strokeColor = ContextCompat.getColor(context, R.color.divider)
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface))
            isClickable = true
            setOnClickListener { onClick() }
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(15), dp(16), dp(15))
        }
        val icon = TextView(this).apply {
            text = "⌕"
            textSize = 26f
            setTextColor(teal)
            gravity = Gravity.CENTER
        }
        row.addView(icon, LinearLayout.LayoutParams(dp(42), dp(50)))
        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, dp(6), 0)
        }
        texts.addView(TextView(this).apply {
            text = label
            textSize = 17f
            setTextColor(textPrimary)
            typeface = Typeface.DEFAULT_BOLD
        })
        texts.addView(TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(2), 0, 0)
        })
        row.addView(texts, LinearLayout.LayoutParams(0, dp(50), 1f))
        row.addView(tv("›", 28f, textSecondary, false), LinearLayout.LayoutParams(dp(24), dp(50)))
        card.addView(row)
        return card
    }

    private fun importButton(): MaterialButton =
        MaterialButton(this).apply {
            text = "CSV তালিকা পরিবর্তন করুন"
            textSize = 15f
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            setIconResource(R.drawable.ic_upload)
            iconTint = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.white))
            backgroundTintList = android.content.res.ColorStateList.valueOf(teal)
            cornerRadius = dp(18)
            setPadding(dp(16), 0, dp(16), 0)
            setOnClickListener { csvPicker.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv", "*/*")) }
        }

    private fun showHome() {
        val root = baseScroll()
        topBrand(root)
        title(root)

        root.addView(actionCard("নাম দিয়ে অনুসন্ধান", "ভোটারের নাম লিখে খুঁজুন") {
            showSearch(SearchField.NAME, "নাম দিয়ে অনুসন্ধান")
        }, LinearLayout.LayoutParams(-1, dp(78)).apply { setMargins(0, 0, 0, dp(10)) })

        root.addView(actionCard("বাবার নাম দিয়ে অনুসন্ধান", "বাবার নাম লিখে খুঁজুন") {
            showSearch(SearchField.FATHER, "বাবার নাম দিয়ে অনুসন্ধান")
        }, LinearLayout.LayoutParams(-1, dp(78)).apply { setMargins(0, 0, 0, dp(10)) })

        root.addView(actionCard("মায়ের নাম দিয়ে অনুসন্ধান", "মায়ের নাম লিখে খুঁজুন") {
            showSearch(SearchField.MOTHER, "মায়ের নাম দিয়ে অনুসন্ধান")
        }, LinearLayout.LayoutParams(-1, dp(78)).apply { setMargins(0, 0, 0, dp(14)) })

        root.addView(importButton(), LinearLayout.LayoutParams(-1, dp(54)))

        root.addView(Space(this), LinearLayout.LayoutParams(1, 0, 1f))
        root.addView(tv("বর্তমান তালিকা: ${db.count()} জন", 12f, textSecondary, false).apply {
            setPadding(0, dp(12), 0, dp(4))
        })
        root.addView(tv("Developed & Designed by", 12f, textSecondary, false))
        root.addView(tv("ছিফাত উল্লাহ", 15f, textPrimary, true).apply {
            setPadding(0, dp(2), 0, 0)
        })
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun showSearch(field: SearchField, screenTitle: String) {
        val root = baseScroll()
        val back = TextView(this).apply {
            text = "‹  $screenTitle"
            textSize = 20f
            setTextColor(textPrimary)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(6), 0, dp(12))
            setOnClickListener { showHome() }
        }
        root.addView(back)

        val layout = TextInputLayout(this).apply {
            hint = when (field) {
                SearchField.NAME -> "এখানে নাম লিখুন"
                SearchField.FATHER -> "এখানে বাবার নাম লিখুন"
                SearchField.MOTHER -> "এখানে মায়ের নাম লিখুন"
            }
            boxCornerRadiusTopStart = dp(18).toFloat()
            boxCornerRadiusTopEnd = dp(18).toFloat()
            boxCornerRadiusBottomStart = dp(18).toFloat()
            boxCornerRadiusBottomEnd = dp(18).toFloat()
            boxStrokeColor = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.divider))
        }
        val input = TextInputEditText(this).apply {
            textSize = 16f
            singleLine = true
            setPadding(dp(16), 0, dp(16), 0)
        }
        layout.addView(input, LinearLayout.LayoutParams(-1, dp(58)))

        root.addView(layout, LinearLayout.LayoutParams(-1, dp(64)).apply { setMargins(0, 0, 0, dp(10)) })

        val searchBtn = MaterialButton(this).apply {
            text = "অনুসন্ধান"
            textSize = 15f
            setIconResource(R.drawable.ic_search)
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            iconTint = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.white))
            backgroundTintList = android.content.res.ColorStateList.valueOf(teal)
            cornerRadius = dp(18)
            setOnClickListener {
                val q = input.text?.toString()?.trim().orEmpty()
                if (q.isBlank()) {
                    input.error = "অনুসন্ধানের জন্য নাম লিখুন"
                    return@setOnClickListener
                }
                val results = db.search(field, q)
                showResults(screenTitle, field, q, results)
            }
        }
        root.addView(searchBtn, LinearLayout.LayoutParams(-1, dp(54)).apply { setMargins(0, 0, 0, dp(14)) })
        root.addView(tv("বাংলা নামের অংশবিশেষ লিখেও অনুসন্ধান করা যাবে।", 12f, textSecondary))
        root.addView(Space(this), LinearLayout.LayoutParams(1, dp(24)))

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun showResults(title: String, field: SearchField, q: String, results: List<Voter>) {
        val root = baseScroll()
        root.addView(TextView(this).apply {
            text = "‹  $title"
            textSize = 20f
            setTextColor(textPrimary)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(6), 0, dp(8))
            setOnClickListener { showSearch(field, title) }
        })
        root.addView(tv("“$q” — ${results.size} জন পাওয়া গেছে", 13f, textSecondary, false).apply {
            gravity = Gravity.START
            setPadding(0, 0, 0, dp(12))
        })
        if (results.isEmpty()) {
            root.addView(tv("কোনো তথ্য পাওয়া যায়নি।", 17f, textPrimary, true).apply {
                setPadding(0, dp(30), 0, 0)
            })
        } else {
            results.forEach { voter ->
                val card = MaterialCardView(this).apply {
                    radius = dp(16).toFloat()
                    cardElevation = dp(1).toFloat()
                    strokeWidth = 1
                    strokeColor = ContextCompat.getColor(context, R.color.divider)
                    setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface))
                    setOnClickListener { showDetails(voter) }
                }
                val box = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(16), dp(13), dp(16), dp(13))
                }
                box.addView(TextView(this).apply {
                    text = voter.name
                    textSize = 16f
                    setTextColor(textPrimary)
                    typeface = Typeface.DEFAULT_BOLD
                })
                box.addView(TextView(this).apply {
                    text = "বাবা: ${voter.father}"
                    textSize = 13f
                    setTextColor(textSecondary)
                    setPadding(0, dp(3), 0, 0)
                })
                box.addView(TextView(this).apply {
                    text = "মা: ${voter.mother}"
                    textSize = 13f
                    setTextColor(textSecondary)
                    setPadding(0, dp(2), 0, 0)
                })
                card.addView(box)
                root.addView(card, LinearLayout.LayoutParams(-1, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, dp(8))
                })
            }
        }
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun showDetails(v: Voter) {
        val message = """
            নাম: ${v.name}

            বাবার নাম: ${v.father}

            মায়ের নাম: ${v.mother}

            ঠিকানা: ${v.address}

            ওয়ার্ড: ${v.ward}

            লিঙ্গ: ${if (v.gender.equals("Female", true)) "নারী" else if (v.gender.equals("Male", true)) "পুরুষ" else v.gender}

            সিরিয়াল: ${v.serial}

            ভোটার নম্বর: ${v.voterNo}
        """.trimIndent()

        MaterialAlertDialogBuilder(this)
            .setTitle("ভোটারের তথ্য")
            .setMessage(message)
            .setPositiveButton("বন্ধ করুন", null)
            .show()
    }

    private fun importCsv(uri: Uri) {
        MaterialAlertDialogBuilder(this)
            .setTitle("নতুন CSV ব্যবহার করবেন?")
            .setMessage("নতুন তালিকা আমদানি করলে বর্তমান তালিকার সব তথ্য মুছে গিয়ে এই CSV-এর তথ্য দেখানো হবে।")
            .setNegativeButton("বাতিল", null)
            .setPositiveButton("আমদানি করুন") { _, _ ->
                try {
                    contentResolver.openInputStream(uri)?.use { input ->
                        db.replaceWithCsv(input)
                    } ?: error("ফাইল খোলা যায়নি")
                    Toast.makeText(this, "নতুন তালিকা সফলভাবে যুক্ত হয়েছে।", Toast.LENGTH_LONG).show()
                    showHome()
                } catch (e: Exception) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("CSV Import ব্যর্থ")
                        .setMessage(e.message ?: "ফাইলটি সঠিক CSV format-এ আছে কি না পরীক্ষা করুন।")
                        .setPositiveButton("ঠিক আছে", null)
                        .show()
                }
            }.show()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    enum class SearchField { NAME, FATHER, MOTHER }
}
