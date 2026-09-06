package com.familybubbles.widget.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class FamilyRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPeople(): List<FamilyPerson> {
        val encrypted = prefs.getString(KEY_PEOPLE_ENCRYPTED, null)
        if (!encrypted.isNullOrBlank()) {
            val decrypted = LocalDataCipher.decrypt(encrypted) ?: return emptyList()
            return parsePeople(decrypted)
        }

        // One-time transparent migration from the original plaintext SharedPreferences value.
        val legacy = prefs.getString(KEY_PEOPLE_LEGACY, null) ?: return emptyList()
        val people = parsePeople(legacy)
        runCatching { persist(people) }
        return people
    }

    fun getPerson(id: String): FamilyPerson? = getPeople().firstOrNull { it.id == id }

    fun save(person: FamilyPerson) {
        val people = getPeople().toMutableList()
        val existing = people.indexOfFirst { it.id == person.id }
        if (existing >= 0) people[existing] = person else people.add(person)
        persist(people)
    }

    fun delete(id: String) {
        val people = getPeople().toMutableList()
        val removed = people.firstOrNull { it.id == id }
        if (removed != null) {
            removed.photoPath?.let { path -> runCatching { File(path).delete() } }
            people.removeAll { it.id == id }
            persist(people)
        }
    }

    private fun persist(people: List<FamilyPerson>) {
        val array = JSONArray()
        people.forEach { person ->
            array.put(
                JSONObject().apply {
                    put("id", person.id)
                    put("name", person.name)
                    put("phone", person.phone)
                    put("photoPath", person.photoPath ?: "")
                }
            )
        }

        val encrypted = LocalDataCipher.encrypt(array.toString())
        prefs.edit()
            .putString(KEY_PEOPLE_ENCRYPTED, encrypted)
            .remove(KEY_PEOPLE_LEGACY)
            .apply()
    }

    private fun parsePeople(raw: String): List<FamilyPerson> = runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    FamilyPerson(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        phone = item.getString("phone"),
                        photoPath = item.optString("photoPath").takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    companion object {
        private const val PREFS_NAME = "family_faces_data"
        private const val KEY_PEOPLE_LEGACY = "people"
        private const val KEY_PEOPLE_ENCRYPTED = "people_encrypted_v1"
    }
}
