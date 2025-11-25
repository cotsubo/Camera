package org.fossify.camera.activities

import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import org.fossify.camera.R
import org.fossify.camera.databinding.ActivityCircleBinding

/**
 * Circle features page with integrated tab navigation.
 */
class CircleActivity : SimpleActivity() {

    private lateinit var binding: ActivityCircleBinding
    private var previousTabIndex = 1 // Default to Photo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCircleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabLayout()
    }

    private fun setupTabLayout() {
        binding.circleModeTab.apply {
            // Select Circle tab (index 2)
            getTabAt(2)?.select()

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    when (tab.position) {
                        0 -> { // Video
                            returnToMainActivity(MainActivity.VIDEO_MODE_INDEX)
                        }
                        1 -> { // Photo
                            returnToMainActivity(MainActivity.PHOTO_MODE_INDEX)
                        }
                        2 -> { // Circle - stay here
                            // Do nothing, already on Circle
                        }
                        3 -> { // History
                            launchHistory()
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }

    private fun returnToMainActivity(mode: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.CAMERA_MODE_KEY, mode)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun launchHistory() {
        startActivity(Intent(this, HistoryActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        returnToMainActivity(MainActivity.PHOTO_MODE_INDEX)
    }
}
