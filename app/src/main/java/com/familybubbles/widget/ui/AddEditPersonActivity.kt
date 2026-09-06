package com.familybubbles.widget.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.familybubbles.widget.data.FamilyPerson
import com.familybubbles.widget.data.FamilyRepository
import com.familybubbles.widget.databinding.ActivityAddEditPersonBinding
import com.familybubbles.widget.widget.FamilyWidgetProvider
import java.io.File
import java.util.UUID

class AddEditPersonActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditPersonBinding
    private lateinit var repository: FamilyRepository
    private lateinit var personId: String
    private var selectedPhotoPath: String? = null

    private val photoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runCatching {
                val photosDir = File(filesDir, "family_photos").apply { mkdirs() }
                val target = File(photosDir, "$personId.jpg")
                contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                } ?: error("Could not open selected image")
                selectedPhotoPath = target.absolutePath
                renderPhoto()
            }.onFailure {
                Toast.makeText(this, "Couldn't use that photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val contactPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val uri = result.data?.data ?: return@registerForActivityResult
        contentResolver.query(
            uri,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (nameIndex >= 0) binding.nameInput.setText(cursor.getString(nameIndex))
                if (numberIndex >= 0) binding.phoneInput.setText(cursor.getString(numberIndex))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditPersonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = FamilyRepository(this)
        val existingId = intent.getStringExtra(EXTRA_PERSON_ID)
        personId = existingId ?: UUID.randomUUID().toString()

        if (existingId != null) {
            val person = repository.getPerson(existingId)
            if (person != null) {
                binding.titleText.text = "Edit family member"
                binding.nameInput.setText(person.name)
                binding.phoneInput.setText(person.phone)
                selectedPhotoPath = person.photoPath
                renderPhoto()
            }
        }

        binding.photoButton.setOnClickListener { photoPicker.launch("image/*") }
        binding.contactButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            contactPicker.launch(intent)
        }
        binding.cancelButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener { savePerson() }
    }

    private fun renderPhoto() {
        val name = binding.nameInput.text?.toString().orEmpty().ifBlank { "?" }
        val bitmap = ImageUtils.loadBitmap(selectedPhotoPath)
        binding.photoView.setImageBitmap(
            if (bitmap != null) ImageUtils.circleCrop(bitmap, 360)
            else ImageUtils.placeholder(360, name.take(2))
        )
    }

    private fun savePerson() {
        val name = binding.nameInput.text?.toString()?.trim().orEmpty()
        val phone = binding.phoneInput.text?.toString()?.trim().orEmpty()
        if (name.isBlank()) {
            binding.nameInput.error = "Add a name"
            return
        }
        if (phone.isBlank()) {
            binding.phoneInput.error = "Add a phone number"
            return
        }

        repository.save(
            FamilyPerson(
                id = personId,
                name = name,
                phone = phone,
                photoPath = selectedPhotoPath
            )
        )
        FamilyWidgetProvider.updateAll(this)
        finish()
    }

    companion object {
        const val EXTRA_PERSON_ID = "person_id"
    }
}
