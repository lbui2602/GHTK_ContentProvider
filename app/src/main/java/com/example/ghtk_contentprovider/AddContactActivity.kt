package com.example.ghtk_contentprovider

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ghtk_contentprovider.databinding.ActivityAddContactBinding


class AddContactActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddContactBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra("id")
        if (id == null) {
            binding.btnAdd.text = "Add"
        } else {
            binding.btnAdd.text = "Update"
            val contact = getContactById(id)
            binding.edtName.setText(contact?.name)
            binding.edtPhone.setText(contact?.phoneNumber)
        }

        binding.btnAdd.setOnClickListener {
            val name = binding.edtName.text.trim().toString()
            val phone = binding.edtPhone.text.trim().toString()
            if (name.isNotEmpty() || phone.isNotEmpty()) {
                if (id == null) {
                    addContact(name, phone)
                } else {
                    updateContact(id, name, phone)
                }
                startActivity(Intent(this, MainActivity::class.java))
            }
        }

    }
    private fun deleteContactByListId(contactIds: List<String>) {
        val contentResolver = contentResolver
        val operations = ArrayList<ContentProviderOperation>()

        for (contactId in contactIds) {
            operations.add(
                ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection("${ContactsContract.Data.CONTACT_ID}=?", arrayOf(contactId))
                    .build()
            )
            operations.add(
                ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection("${ContactsContract.RawContacts.CONTACT_ID}=?", arrayOf(contactId))
                    .build()
            )
        }

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            Toast.makeText(this, "Các liên hệ đã được xóa", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Xóa các liên hệ thất bại", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateContact(contactId: String, newDisplayName: String?, newPhoneNumber: String?) {
        val contentResolver = contentResolver
        val operations = ArrayList<ContentProviderOperation>()

        newDisplayName?.let {
            operations.add(
                ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                        arrayOf(contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    )
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, it)
                    .build()
            )
        }

        newPhoneNumber?.let {
            operations.add(
                ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                        arrayOf(contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, it)
                    .build()
            )
        }

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            Toast.makeText(this, "Liên hệ đã được cập nhật", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Cập nhật liên hệ thất bại", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getContactById(contactId: String): Contact? {
        val contentResolver: ContentResolver = contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        cursor?.let {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val name = it.getString(nameIndex)
                val phoneNumber = it.getString(numberIndex)
                it.close()
                return Contact(contactId, name, phoneNumber)
            }
            it.close()
        }

        return null
    }

    private fun addContact(displayName: String, phoneNumber: String) {
        val contentResolver = contentResolver
        val operations = ArrayList<ContentProviderOperation>()
        operations.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )
        operations.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .build()
        )
        operations.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build()
        )
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            Toast.makeText(this, "Liên hệ đã được thêm", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Thêm liên hệ thất bại", Toast.LENGTH_SHORT).show()
        }
    }
}
