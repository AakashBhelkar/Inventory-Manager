package com.inventoryapp

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.inventoryapp.viewmodel.InventoryViewModel

class RackSelectionDialog : DialogFragment() {

    private lateinit var viewModel: InventoryViewModel

    companion object {
        fun newInstance(scannedData: String, scanType: String): RackSelectionDialog {
            val fragment = RackSelectionDialog()
            val args = Bundle().apply {
                putString("scanned_data", scannedData)
                putString("scan_type", scanType)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = ViewModelProvider(requireActivity())[InventoryViewModel::class.java]

        val scannedData = arguments?.getString("scanned_data") ?: ""
        val scanType = arguments?.getString("scan_type") ?: ""

        val view = layoutInflater.inflate(R.layout.dialog_rack_selection, null)
        val rackInput = view.findViewById<TextInputEditText>(R.id.etRackLocation)

        return AlertDialog.Builder(requireContext())
            .setTitle("Select Rack Location")
            .setMessage("Scanned: $scannedData\nOperation: $scanType")
            .setView(view)
            .setPositiveButton("Confirm") { _, _ ->
                val rackLocation = rackInput.text.toString().trim()
                if (rackLocation.isNotEmpty()) {
                    viewModel.processInventoryOperation(scannedData, scanType, rackLocation)
                } else {
                    Toast.makeText(context, "Please enter rack location", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}