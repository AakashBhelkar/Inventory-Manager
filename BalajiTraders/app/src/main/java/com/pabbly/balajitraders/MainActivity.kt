package com.inventoryapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.appbar.MaterialToolbar
import com.inventoryapp.databinding.ActivityMainBinding
import com.inventoryapp.scanner.ScannerActivity
import com.inventoryapp.settings.SettingsActivity
import com.inventoryapp.viewmodel.InventoryViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: InventoryViewModel

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openScanner(currentScanType)
        } else {
            Toast.makeText(this, "Camera permission is required for scanning", Toast.LENGTH_LONG).show()
        }
    }

    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val scannedData = result.data?.getStringExtra("scanned_data")
            val scanType = result.data?.getStringExtra("scan_type")
            scannedData?.let { data ->
                scanType?.let { type ->
                    showRackSelectionDialog(data, type)
                }
            }
        }
    }

    private var currentScanType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[InventoryViewModel::class.java]

        setupToolbar()
        setupButtons()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupButtons() {
        binding.btnStockIn.setOnClickListener {
            currentScanType = "STOCK_IN"
            checkCameraPermissionAndScan()
        }

        binding.btnStockOut.setOnClickListener {
            currentScanType = "STOCK_OUT"
            checkCameraPermissionAndScan()
        }
    }

    private fun checkCameraPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openScanner(currentScanType)
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openScanner(scanType: String) {
        val intent = Intent(this, ScannerActivity::class.java).apply {
            putExtra("scan_type", scanType)
        }
        scannerLauncher.launch(intent)
    }

    private fun showRackSelectionDialog(scannedData: String, scanType: String) {
        val dialog = RackSelectionDialog.newInstance(scannedData, scanType)
        dialog.show(supportFragmentManager, "RackSelectionDialog")
    }

    private fun observeViewModel() {
        viewModel.operationResult.observe(this) { result ->
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnStockIn.isEnabled = !isLoading
            binding.btnStockOut.isEnabled = !isLoading
        }
    }
}