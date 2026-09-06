package com.familybubbles.widget.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.familybubbles.widget.data.FamilyRepository
import com.familybubbles.widget.databinding.ActivityMainBinding
import com.familybubbles.widget.widget.FamilyWidgetProvider

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: FamilyRepository
    private lateinit var adapter: PersonAdapter

    private val callPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { updatePermissionCard() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = FamilyRepository(this)
        adapter = PersonAdapter(
            onEdit = { person -> openEditor(person.id) },
            onDelete = { person -> confirmDelete(person.id, person.name) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.addButton.setOnClickListener { openEditor(null) }
        binding.permissionButton.setOnClickListener {
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPeople()
        updatePermissionCard()
    }

    private fun refreshPeople() {
        val people = repository.getPeople()
        adapter.submitList(people)
        binding.emptyText.visibility = if (people.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (people.isEmpty()) View.GONE else View.VISIBLE
        FamilyWidgetProvider.updateAll(this)
    }

    private fun updatePermissionCard() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        binding.permissionStatus.text = if (granted) {
            "Ready. Tapping a face on the widget will immediately place the call."
        } else {
            "Permission is needed before the widget can place a call without opening the dialler."
        }
        binding.permissionButton.visibility = if (granted) View.GONE else View.VISIBLE
    }

    private fun openEditor(personId: String?) {
        startActivity(Intent(this, AddEditPersonActivity::class.java).apply {
            personId?.let { putExtra(AddEditPersonActivity.EXTRA_PERSON_ID, it) }
        })
    }

    private fun confirmDelete(id: String, name: String) {
        AlertDialog.Builder(this)
            .setTitle("Remove $name?")
            .setMessage("They will disappear from the widget.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Remove") { _, _ ->
                repository.delete(id)
                refreshPeople()
            }
            .show()
    }
}
