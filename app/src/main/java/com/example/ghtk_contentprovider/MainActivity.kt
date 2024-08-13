package com.example.ghtk_contentprovider

import android.Manifest
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ghtk_contentprovider.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(),OnClickItem {

    private val CONTACT_PERMISSION_CODE = 1
    private lateinit var binding : ActivityMainBinding
    val listDelete = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
                CONTACT_PERMISSION_CODE
            )
        } else {
            readContacts()
        }
        binding.convertButton.setOnClickListener{
            convertContacts()
        }
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddContactActivity::class.java))
        }
        binding.btnDelete.setOnClickListener {
            deleteContactByListId(listDelete)
            Log.e("TAG",listDelete.size.toString())
            readContacts()
        }
    }
    private fun deleteContactByListId(contactIds: List<String>) {
        val contentResolver = contentResolver
        val operations = ArrayList<ContentProviderOperation>()

        for (contactId in contactIds) {
            operations.add(
                ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection("${ContactsContract.RawContacts.CONTACT_ID}=?", arrayOf(contactId))
                    .build()
            )

            operations.add(
                ContentProviderOperation.newDelete(ContactsContract.Contacts.CONTENT_URI)
                    .withSelection("${ContactsContract.Contacts._ID}=?", arrayOf(contactId))
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



    private fun convertContacts() {
        val contentResolver: ContentResolver = contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        cursor?.let {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val rawContactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val rawContactId = it.getString(rawContactIdIndex)
                var phoneNumber = it.getString(numberIndex)

                if (phoneNumber.startsWith("016")) {
                    phoneNumber = "03" + phoneNumber.substring(3)
                }
                val values = ContentValues()
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)

                val where = "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
                val whereArgs = arrayOf(rawContactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)

                contentResolver.update(
                    ContactsContract.Data.CONTENT_URI,
                    values,
                    where,
                    whereArgs
                )
            }
            it.close()
        }
        readContacts()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACT_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readContacts()
            } else {
            }
        }
    }

    private fun readContacts() {
        val contactList = mutableListOf<Contact>()

        val contentResolver: ContentResolver = contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.let {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex)
                val phoneNumber = it.getString(numberIndex)
                contactList.add(Contact(id, name, phoneNumber))
            }
            it.close()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = ContactAdapter(contactList,this)
    }

    override fun onClickItem(contact: Contact) {
        val intent = Intent(this, AddContactActivity::class.java)
        intent.putExtra("id",contact.id)
        startActivity(intent)
    }

    override fun addListDelete(contactId: String) {
        listDelete.add(contactId)
    }

    override fun deleteListDelete(contactId: String) {
        listDelete.remove(contactId)
    }

}