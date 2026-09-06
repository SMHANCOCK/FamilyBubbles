package com.familybubbles.widget.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class FamilyRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPeople(): List<FamilyPerson> {
        val raw = prefs.getString(KEY_PEOPLE, null) ?: return emptyList()
        return runCatching {
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
        prefs.edit().putString(KEY_PEOPLE, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "family_faces_data"
        private const val KEY_PEOPLE = "people"
    }
}
