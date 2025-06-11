package com.inventoryapp.data

import android.content.Context
import android.provider.Settings
import com.inventoryapp.database.InventoryDatabase
import com.inventoryapp.model.InventoryTransaction
import com.inventoryapp.model.WebhookConfig
import com.inventoryapp.network.WebhookService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventoryRepository(private val context: Context) {

    private val database = InventoryDatabase.getDatabase(context)
    private val transactionDao = database.transactionDao()
    private val webhookDao = database.webhookDao()
    private val webhookService = WebhookService()

    fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    suspend fun insertTransaction(transaction: InventoryTransaction) {
        withContext(Dispatchers.IO) {
            transactionDao.insert(transaction)
        }
    }

    suspend fun syncPendingTransactions() {
        withContext(Dispatchers.IO) {
            val pendingTransactions = transactionDao.getPendingTransactions()
            val webhooks = webhookDao.getAllWebhooks()

            for (transaction in pendingTransactions) {
                val relevantWebhooks = webhooks.filter { webhook ->
                    (transaction.operation == "STOCK_IN" && webhook.isStockInEnabled) ||
                            (transaction.operation == "STOCK_OUT" && webhook.isStockOutEnabled)
                }

                var allSuccess = true
                for (webhook in relevantWebhooks) {
                    try {
                        webhookService.sendTransaction(webhook.url, transaction)
                    } catch (e: Exception) {
                        allSuccess = false
                        break
                    }
                }

                if (allSuccess) {
                    transactionDao.markAsSynced(transaction.id)
                }
            }
        }
    }

    suspend fun getAllWebhooks(): List<WebhookConfig> {
        return withContext(Dispatchers.IO) {
            webhookDao.getAllWebhooks()
        }
    }

    suspend fun insertWebhook(webhook: WebhookConfig) {
        withContext(Dispatchers.IO) {
            webhookDao.insert(webhook)
        }
    }

    suspend fun deleteWebhook(webhook: WebhookConfig) {
        withContext(Dispatchers.IO) {
            webhookDao.delete(webhook)
        }
    }

    suspend fun testWebhook(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                webhookService.testWebhook(url)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}