package org.fossify.camera.activities

import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import org.fossify.camera.R
import org.fossify.camera.databinding.ActivityHistoryBinding

/**
 * History features page with integrated tab navigation.
 */
class HistoryActivity : SimpleActivity() {

    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabLayout()
    }

    private fun setupTabLayout() {
        binding.historyModeTab.apply {
            // Select History tab (index 3)
            getTabAt(3)?.select()

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    when (tab.position) {
                        0 -> { // Video
                            returnToMainActivity(MainActivity.VIDEO_MODE_INDEX)
                        }
                        1 -> { // Photo
                            returnToMainActivity(MainActivity.PHOTO_MODE_INDEX)
                        }
                        2 -> { // Circle
                            launchCircle()
                        }
                        3 -> { // History - stay here
                            // Do nothing, already on History
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

    private fun launchCircle() {
        startActivity(Intent(this, CircleActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        returnToMainActivity(MainActivity.PHOTO_MODE_INDEX)
    }
}
