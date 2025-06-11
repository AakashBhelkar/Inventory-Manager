package com.inventoryapp.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.inventoryapp.R
import com.inventoryapp.databinding.ActivitySettingsBinding
import com.inventoryapp.model.WebhookConfig
import com.inventoryapp.viewmodel.SettingsViewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private lateinit var webhookAdapter: WebhookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!checkPassword()) {
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        setupRecyclerView()
        setupButtons()
        observeViewModel()

        viewModel.loadWebhooks()
    }

    private fun checkPassword(): Boolean {
        val input = TextInputEditText(this)
        var passwordCorrect = false

        val dialog = AlertDialog.Builder(this)
            .setTitle("Enter Password")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val enteredPassword = input.text.toString()
                if (viewModel.verifyPassword(enteredPassword)) {
                    passwordCorrect = true
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .create()

        dialog.show()
        return passwordCorrect
    }

    private fun setupRecyclerView() {
        webhookAdapter = WebhookAdapter(
            onDeleteClick = { webhook ->
                viewModel.deleteWebhook(webhook)
            },
            onTestClick = { webhook ->
                viewModel.testWebhook(webhook.url)
            }
        )

        binding.rvWebhooks.apply {
            layoutManager = LinearLayoutManager(this@SettingsActivity)
            adapter = webhookAdapter
        }
    }

    private fun setupButtons() {
        binding.btnAddWebhook.setOnClickListener {
            showAddWebhookDialog()
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun showAddWebhookDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_webhook, null)
        val urlInput = view.findViewById<TextInputEditText>(R.id.etWebhookUrl)
        val stockInCheckbox = view.findViewById<androidx.appcompat.widget.AppCompatCheckBox>(R.id.cbStockIn)
        val stockOutCheckbox = view.findViewById<androidx.appcompat.widget.AppCompatCheckBox>(R.id.cbStockOut)

        AlertDialog.Builder(this)
            .setTitle("Add Webhook")
            .setView(view)
            .setPositiveButton("Add") { _, _ ->
                val url = urlInput.text.toString().trim()
                if (url.isNotEmpty()) {
                    val webhook = WebhookConfig(
                        url = url,
                        isStockInEnabled = stockInCheckbox.isChecked,
                        isStockOutEnabled = stockOutCheckbox.isChecked
                    )
                    viewModel.addWebhook(webhook)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val currentPasswordInput = view.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val newPasswordInput = view.findViewById<TextInputEditText>(R.id.etNewPassword)
        val confirmPasswordInput = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(view)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()

                if (newPassword == confirmPassword) {
                    viewModel.changePassword(currentPassword, newPassword)
                } else {
                    Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.webhooks.observe(this) { webhooks ->
            webhookAdapter.submitList(webhooks)
        }

        viewModel.message.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}