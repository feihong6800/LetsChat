package com.example.letschat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactFragment extends Fragment {

    private View contactView;
    private RecyclerView contactsList;
    private DatabaseReference contactRef, userRef;
    private FirebaseAuth mAuth;
    private String currentID;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contactView = inflater.inflate(R.layout.fragment_contact, container, false);
        contactsList = (RecyclerView) contactView.findViewById(R.id.contacts_list);
        contactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        currentID = mAuth.getCurrentUser().getUid();
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return contactView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(contactRef, Contact.class).build();
        final FirebaseRecyclerAdapter<Contact, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contact model) {
                final String userID = getRef(position).getKey();

                userRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.child("userState").hasChild("state")) {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("ONLINE")) {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);

                                } else if (state.equals("OFFLINE")) {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);

                                }
                            } else {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }

                            if (dataSnapshot.hasChild("image")) {
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.ic_account).into(holder.profileImage);

                            } else {
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext()).inflate(R.layout.users_display_layout, group, false);
                ContactsViewHolder holder = new ContactsViewHolder(view);
                return holder;
            }
        };
        contactsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.username);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.profile);
            onlineIcon = (ImageView) itemView.findViewById(R.id.online_status);
        }
    }
}