package com.example.chatapp;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<Message> messageList;
    String currentUserId;

    public MessageAdapter(List<Message> messageList) {
        this.messageList   = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.tvMessage.setText(message.text);

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        holder.tvTime.setText(sdf.format(new Date(message.timestamp)));

        boolean isMe = message.senderId.equals(currentUserId);

        if (isMe) {
            // My message — blue bubble, right aligned
            holder.tvMessage.setBackgroundResource(R.drawable.bubble_bg);
            holder.tvMessage.setTextColor(0xFFFFFFFF);
            holder.tvSenderName.setVisibility(View.GONE);

            // Align everything to right
            holder.container.setGravity(Gravity.END);
            holder.tvTime.setGravity(Gravity.END);

        } else {
            // Other person — grey bubble, left aligned
            holder.tvMessage.setBackgroundResource(R.drawable.bubble_bg_grey);
            holder.tvMessage.setTextColor(0xFF212121);
            holder.tvSenderName.setVisibility(View.VISIBLE);
            holder.tvSenderName.setText(message.senderName);

            // Align everything to left
            holder.container.setGravity(Gravity.START);
            holder.tvTime.setGravity(Gravity.START);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvSenderName, tvTime;
        LinearLayout container;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage    = itemView.findViewById(R.id.tvMessage);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvTime       = itemView.findViewById(R.id.tvTime);
            container    = (LinearLayout) itemView;
        }
    }
}