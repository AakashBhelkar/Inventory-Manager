package com.inventoryapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.inventoryapp.data.InventoryRepository
import com.inventoryapp.model.InventoryTransaction
import kotlinx.coroutines.launch
import java.util.*

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository(application)

    private val _operationResult = MutableLiveData<String>()
    val operationResult: LiveData<String> = _operationResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun processInventoryOperation(barcode: String, operation: String, rackLocation: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val transaction = InventoryTransaction(
                    barcode = barcode,
                    operation = operation,
                    rackLocation = rackLocation,
                    timestamp = Date(),
                    deviceId = repository.getDeviceId(),
                    synced = false
                )

                repository.insertTransaction(transaction)
                repository.syncPendingTransactions()

                _operationResult.value = "Operation completed successfully"
            } catch (e: Exception) {
                _operationResult.value = "Operation failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}