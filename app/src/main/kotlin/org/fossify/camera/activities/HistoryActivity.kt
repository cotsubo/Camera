package org.fossify.camera.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.fossify.camera.R
import org.fossify.camera.adapters.MediaAdapter
import org.fossify.camera.database.SimpleMediaStorage
import org.fossify.camera.databinding.ActivityHistoryBinding
import org.fossify.camera.models.CapturedMedia
import java.io.File

/**
 * History features page with integrated tab navigation.
 * Displays all captured photos and videos from isolated storage.
 */
class HistoryActivity : SimpleActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var mediaStorage: SimpleMediaStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaStorage = SimpleMediaStorage.getInstance(this)
        setupRecyclerView()
        setupTabLayout()
        loadMedia()
    }

    private fun setupRecyclerView() {
        mediaAdapter = MediaAdapter { media ->
            openMedia(media)
        }

        binding.mediaRecyclerView.apply {
            layoutManager = GridLayoutManager(this@HistoryActivity, 3)
            adapter = mediaAdapter
        }
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

    private fun loadMedia() {
        lifecycleScope.launch {
            mediaStorage.getAllMedia().collectLatest { mediaList ->
                if (mediaList.isEmpty()) {
                    binding.mediaRecyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                } else {
                    binding.mediaRecyclerView.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                    mediaAdapter.submitList(mediaList)
                }
            }
        }
    }

    private fun openMedia(media: CapturedMedia) {
        val file = File(media.filePath)
        if (!file.exists()) {
            showErrorToast(getString(R.string.file_not_found))
            return
        }

        val uri = androidx.core.content.FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, media.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            showErrorToast(getString(R.string.no_app_found))
        }
    }

    private fun showErrorToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
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
