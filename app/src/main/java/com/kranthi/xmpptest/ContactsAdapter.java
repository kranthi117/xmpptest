package com.kranthi.xmpptest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kranthi on 16/10/16.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactHolder> {

    private List<Contact> contacts = new ArrayList<>(2);

    public void initData() {
        Contact contact = new Contact();
        contact.name = "kranthi@13.78.120.174";
        contacts.add(contact);
        Contact contact1 = new Contact();
        contact1.name = "sai@13.78.120.174";
        contacts.add(contact1);
        notifyDataSetChanged();
    }
    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, null, false);
        return new ContactHolder(layout);
    }

    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return (contacts == null) ? 0 : contacts.size();
    }

    public class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView name;
        private View itemView;

        public ContactHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.contact_name);
            itemView.setOnClickListener(this);
            this.itemView = itemView;
        }

        public void bind(Contact contact) {
            name.setText(contact.name);
            itemView.setTag(R.id.contact, contact);
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Contact contact = (Contact) v.getTag(R.id.contact);
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra("to", contact.name);
            context.startActivity(chatIntent);
        }
    }
}
