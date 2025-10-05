package com.dharmabit.hikemate.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dharmabit.hikemate.activities.RecordHikeActivity
import com.dharmabit.hikemate.adapters.HikeAdapter
import com.dharmabit.hikemate.data.database.HikeDatabase
import com.dharmabit.hikemate.data.repository.HikeRepository
import com.dharmabit.hikemate.databinding.FragmentHomeBinding
import com.dharmabit.hikemate.utils.formatDistance
import com.dharmabit.hikemate.utils.getFormattedStopWatchTime
import com.dharmabit.hikemate.viewmodels.HomeViewModel
import com.dharmabit.hikemate.viewmodels.ViewModelFactory
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var hikeAdapter: HikeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeData()
        loadQuickStats()
    }

    private fun setupViewModel() {
        val database = HikeDatabase.getDatabase(requireContext())
        val repository = HikeRepository(database.getHikeDao())
        homeViewModel = ViewModelProvider(this, ViewModelFactory(repository))[HomeViewModel::class.java]
    }

    private fun setupRecyclerView() {
        hikeAdapter = HikeAdapter { hike ->
            // Handle hike item click
            val intent = Intent(requireContext(), HikeDetailActivity::class.java).apply {
                putExtra(HikeDetailActivity.EXTRA_HIKE_ID, hike.id)
            }
            startActivity(intent)
        }

        binding.rvRecentHikes.apply {
            adapter = hikeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.btnStartHike.setOnClickListener {
            requestLocationPermissions {
                startActivity(Intent(requireContext(), RecordHikeActivity::class.java))
            }
        }
    }

    private fun observeData() {
        homeViewModel.allHikes.observe(viewLifecycleOwner) { hikes ->
            // Show only recent hikes (last 5)
            hikeAdapter.submitList(hikes.take(5))

            binding.tvNoHikes.visibility = if (hikes.isEmpty()) View.VISIBLE else View.GONE
            binding.rvRecentHikes.visibility = if (hikes.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun loadQuickStats() {
        lifecycleScope.launch {
            val database = HikeDatabase.getDatabase(requireContext())
            val repository = HikeRepository(database.getHikeDao())

            val totalDistance = repository.getTotalDistance()
            val totalDuration = repository.getTotalDuration()
            val hikeCount = repository.getHikeCount()

            binding.tvTotalDistance.text = totalDistance.formatDistance()
            binding.tvTotalTime.text = totalDuration.getFormattedStopWatchTime()
            binding.tvTotalHikes.text = hikeCount.toString()
        }
    }

    private fun requestLocationPermissions(onPermissionGranted: () -> Unit) {
        Dexter.withContext(requireContext())
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        onPermissionGranted()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .check()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}