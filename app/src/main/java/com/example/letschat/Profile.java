package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private String receiverID, senderID, currentState;
    private CircleImageView profilePic;
    private TextView username, status;
    private Button btn_Request, btn_Decline;
    private DatabaseReference userRef, chatRef, contactRef, notificationRef;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        mToolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        receiverID = getIntent().getExtras().get("visit_user_id").toString();
        senderID = mAuth.getCurrentUser().getUid();

        profilePic = (CircleImageView) findViewById(R.id.visit_profile_image);
        username = (TextView) findViewById(R.id.visit_user_name);
        status = (TextView) findViewById(R.id.visit_profile_status);
        btn_Request = (Button) findViewById(R.id.send_message_request_button);
        btn_Decline = (Button) findViewById(R.id.decline_message_request_button);
        currentState = "new";

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        userRef.child(receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.ic_account).into(profilePic);
                    username.setText(userName);
                    status.setText(userstatus);

                    ManageChatRequests();
                } else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();

                    username.setText(userName);
                    status.setText(userstatus);

                    ManageChatRequests();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequests() {
        chatRef.child(senderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverID)) {
                    String request_type = dataSnapshot.child(receiverID).child("request_type").getValue().toString();

                    if (request_type.equals("sent")) {
                        currentState = "request_sent";
                        btn_Request.setText("CANCEL REQUEST");
                    } else if (request_type.equals("received")) {
                        currentState = "request_received";
                        btn_Request.setText("ACCEPT Request");

                        btn_Decline.setVisibility(View.VISIBLE);
                        btn_Decline.setEnabled(true);

                        btn_Decline.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CancelChatRequest();
                            }
                        });
                    }
                } else {
                    contactRef.child(senderID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverID)) {
                                currentState = "friends";
                                btn_Request.setText("Remove this Contact");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (!senderID.equals(receiverID)) {
            btn_Request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btn_Request.setEnabled(false);

                    if (currentState.equals("new")) {
                        SendChatRequest();
                    }
                    if (currentState.equals("request_sent")) {
                        CancelChatRequest();
                    }
                    if (currentState.equals("request_received")) {
                        AcceptChatRequest();
                    }
                    if (currentState.equals("friends")) {
                        RemoveSpecificContact();
                    }
                }
            });
        } else {
            btn_Request.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        contactRef.child(senderID).child(receiverID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactRef.child(receiverID).child(senderID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                btn_Request.setEnabled(true);
                                currentState = "new";
                                btn_Request.setText("SEND MESSASGE");

                                btn_Decline.setVisibility(View.INVISIBLE);
                                btn_Decline.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptChatRequest() {
        contactRef.child(senderID).child(receiverID).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactRef.child(receiverID).child(senderID).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                chatRef.child(senderID).child(receiverID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            chatRef.child(receiverID).child(senderID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    btn_Request.setEnabled(true);
                                                    currentState = "friends";
                                                    btn_Request.setText("REMOVE THIS CONTACT");

                                                    btn_Decline.setVisibility(View.INVISIBLE);
                                                    btn_Decline.setEnabled(false);
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void CancelChatRequest() {
        chatRef.child(senderID).child(receiverID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    chatRef.child(receiverID).child(senderID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                btn_Request.setEnabled(true);
                                currentState = "new";
                                btn_Request.setText("SEND MESSAGE");

                                btn_Decline.setVisibility(View.INVISIBLE);
                                btn_Decline.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void SendChatRequest() {
        chatRef.child(senderID).child(receiverID).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    chatRef.child(receiverID).child(senderID).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                chatNotificationMap.put("from", senderID);
                                chatNotificationMap.put("type", "request");

                                notificationRef.child(receiverID).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            btn_Request.setEnabled(true);
                                            currentState = "request_sent";
                                            btn_Request.setText("CANCEL CHAT REQUEST");
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, AddFriend.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}