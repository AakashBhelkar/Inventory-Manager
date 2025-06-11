# Create all directories
New-Item -ItemType Directory -Path "app\src\main\java\com\pabbly\balajitraders\scanner" -Force
New-Item -ItemType Directory -Path "app\src\main\java\com\pabbly\balajitraders\settings" -Force
New-Item -ItemType Directory -Path "app\src\main\java\com\pabbly\balajitraders\viewmodel" -Force
New-Item -ItemType Directory -Path "app\src\main\java\com\pabbly\balajitraders\data" -Force
New-Item -ItemType Directory -Path "app\src\main\java\com\pabbly\balajitraders\database" -Force
New-Item -ItemType Directory -Path "app\src\main\java\com\pabbly\balajitraders\model" -Force
New-Item -ItemType Directory -Path "app\src\main\java\com\pabbly\balajitraders\network" -Force
New-Item -ItemType Directory -Path "app\src\main\res\menu" -Force
New-Item -ItemType Directory -Path "app\src\main\res\drawable" -Force
New-Item -ItemType Directory -Path "app\src\main\res\xml" -Force

# Create MainActivity.kt
@'
package com.pabbly.balajitraders

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.pabbly.balajitraders.databinding.ActivityMainBinding
import com.pabbly.balajitraders.scanner.ScannerActivity
import com.pabbly.balajitraders.settings.SettingsActivity
import com.pabbly.balajitraders.viewmodel.InventoryViewModel

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
'@ | Out-File -FilePath "app\src\main\java\com\pabbly\balajitraders\MainActivity.kt" -Encoding UTF8

# Create activity_main.xml
@'
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="0dp"
        android:layout_height="?attr/actionBarSize"
        android:background="?attr/colorPrimary"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:menu="@menu/main_menu"
        app:title="Inventory Management"
        app:titleTextColor="@android:color/white" />

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:gravity="center"
        android:orientation="vertical"
        android:padding="32dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/toolbar">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnStockIn"
            android:layout_width="match_parent"
            android:layout_height="80dp"
            android:layout_marginBottom="24dp"
            android:text="Stock In"
            android:textSize="18sp"
            app:cornerRadius="12dp"
            app:icon="@drawable/ic_add"
            app:iconGravity="textStart"
            app:iconSize="24dp" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnStockOut"
            android:layout_width="match_parent"
            android:layout_height="80dp"
            android:text="Stock Out"
            android:textSize="18sp"
            app:cornerRadius="12dp"
            app:icon="@drawable/ic_remove"
            app:iconGravity="textStart"
            app:iconSize="24dp" />

    </LinearLayout>

</androidx.constraintlayout.widget.ConstraintLayout>
'@ | Out-File -FilePath "app\src\main\res\layout\activity_main.xml" -Encoding UTF8

Write-Host "All files created successfully!" -ForegroundColor Green
