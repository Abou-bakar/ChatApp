package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText etMessage;
    Button btnSend;
    TextView tvLogout, tvEmpty;

    FirebaseAuth auth;
    DatabaseReference messagesRef;
    DatabaseReference usersRef;

    MessageAdapter adapter;
    List<Message> messageList;

    String currentUserId;
    String currentUserName = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth          = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        messagesRef   = FirebaseDatabase.getInstance().getReference("Messages");
        usersRef      = FirebaseDatabase.getInstance().getReference("Users");

        recyclerView = findViewById(R.id.recyclerView);
        etMessage    = findViewById(R.id.etMessage);
        btnSend      = findViewById(R.id.btnSend);
        tvLogout     = findViewById(R.id.tvLogout);
        tvEmpty      = findViewById(R.id.tvEmpty);

        messageList = new ArrayList<>();
        adapter     = new MessageAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Get current user name
        usersRef.child(currentUserId).child("name")
                .get().addOnSuccessListener(snapshot -> {
                    if (snapshot.getValue(String.class) != null) {
                        currentUserName = snapshot.getValue(String.class);
                    }
                });

        // Listen for messages in real time
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Message message = ds.getValue(Message.class);
                    if (message != null) messageList.add(message);
                }
                adapter.notifyDataSetChanged();

                // Show empty state or message list
                if (messageList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        // Send message
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;

            Message message = new Message(
                    currentUserId,
                    currentUserName,
                    text,
                    System.currentTimeMillis()
            );

            messagesRef.push().setValue(message);
            etMessage.setText("");
        });

        // Logout
        tvLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // Prevent going back to chat after logout
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}