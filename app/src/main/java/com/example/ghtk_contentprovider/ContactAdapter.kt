package com.example.ghtk_contentprovider

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ghtk_contentprovider.databinding.ItemContactBinding

class ContactAdapter(
    private val contactList: List<Contact>,
    private val onClickItem: OnClickItem
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.cbDelete.setOnCheckedChangeListener { _, isChecked ->
                val contact = contactList[adapterPosition]
                if (isChecked) {
                    onClickItem.addListDelete(contact.id)
                } else {
                    onClickItem.deleteListDelete(contact.id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.binding.nameTextView.text = contact.name
        holder.binding.phoneTextView.text = contact.phoneNumber
        holder.binding.llItem.setOnClickListener {
            onClickItem.onClickItem(contact)
        }
    }

    override fun getItemCount(): Int = contactList.size
}

interface OnClickItem {
    fun onClickItem(contact: Contact)
    fun addListDelete(contactId: String)
    fun deleteListDelete(contactId: String)
}
