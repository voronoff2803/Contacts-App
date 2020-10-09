package com.example.contactsapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.contact_cell.view.*


class MainActivity : AppCompatActivity() {
    var contactList : MutableList<Contact> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fetchAllContacts()

        val text = resources.getQuantityString(R.plurals.welcome_messages, contactList.count() /* для подсчёта кол-ва */ , contactList.count() /* для плейсхолдера */)
        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()

        val onClick: (Contact) -> Unit = {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+ it.phoneNumber))
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        contact_list.adapter = ContactAdapter(items = contactList, ctx = this, onClick = onClick)
    }

    data class Contact(val name: String, val phoneNumber: String)

    fun Context.fetchAllContacts() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            val text = "Нет контактов или нет разрешения на чтение"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(applicationContext, text, duration)
            toast.show()

        } else {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )
                .use { cursor ->
                    if (cursor == null) return
                    while (cursor.moveToNext()) {
                        val name =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                                ?: "N/A"
                        val phoneNumber =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                ?: "N/A"

                        contactList.add(Contact(name, phoneNumber))
                    }
                }
        }
    }

    class ContactAdapter(items:List<Contact>, ctx: Context, val onClick: (Contact) -> Unit) : RecyclerView.Adapter<ContactAdapter.ViewHolder>(){

        var list = items
        var context = ctx

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactAdapter.ViewHolder {
            val holder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.contact_cell,parent,false))
            holder.root.setOnClickListener {
                onClick(list[holder.adapterPosition])
            }
            return holder
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ContactAdapter.ViewHolder, position: Int) {
            holder.bind(list[position])
        }

        class ViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
            fun bind(contact: Contact) {
                with(root) {
                    nameLabel.text = contact.name
                    descriptionLabel.text = contact.phoneNumber
                    if (Math.random() < 0.5) {
                        avatarImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher_foreground));
                    } else {
                        avatarImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher_background));
                    }
                }
            }
        }

    }
}

