package com.example.growmapapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.view.ViewGroup;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SupportActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private EditText etMessage;
    private ImageButton btnSendMessage;
    private TextView userAvatar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GenerativeModelFutures model;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        // Aqui vocês trazendo o acesso do LLM do nosso bot, eu coloquei o gemini pra teste, ai vcs alterem esta parte do código
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", "YOUR_API_KEY");
        model = GenerativeModelFutures.from(gm);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userAvatar = findViewById(R.id.userAvatar);

        loadUserData();
        setupNavbar();
        setupFAQ();
        setupChat();
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("user").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("fullname");
                    if (name != null && !name.isEmpty() && userAvatar != null) {
                        userAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                    }
                }
            });
        }
    }

    private void setupNavbar() {
        View btnBack = findViewById(R.id.btnBack);
        ImageView btnThemeToggle = findViewById(R.id.btnThemeToggle);
        View btnDashboard = findViewById(R.id.btnDashboard);
        View btnCursos = findViewById(R.id.btnCursos);
        
        if (userAvatar != null) {
            userAvatar.setOnClickListener(v -> startActivity(new Intent(this, GerenciarUser.class)));
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        updateThemeIcon(btnThemeToggle);

        if (btnThemeToggle != null) {
            btnThemeToggle.setOnClickListener(v -> {
                int mode = AppCompatDelegate.getDefaultNightMode();
                if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                recreate();
            });
        }

        if (btnDashboard != null) {
            btnDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        if (btnCursos != null) {
            btnCursos.setOnClickListener(v -> {
                startActivity(new Intent(this, RoadmapActivity.class));
            });
        }
    }

    private void setupFAQ() {
        LinearLayout faqContainer = findViewById(R.id.faqContainer);
        String[][] faqs = {
            {"Como altero minha senha?", "Você pode alterar sua senha na tela de Gerenciamento de Usuário clicando no seu perfil."},
            {"Onde vejo meus cursos?", "Todos os seus cursos estão listados na aba 'Cursos' do menu superior."},
            {"Como entrar em contato?", "Você pode usar este chat ou enviar um e-mail para suporte@growmap.com."}
        };

        for (String[] faq : faqs) {
            View itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, faqContainer, false);
            TextView text1 = itemView.findViewById(android.R.id.text1);
            TextView text2 = itemView.findViewById(android.R.id.text2);

            text1.setText(faq[0]);
            text1.setTextColor(getResources().getColor(R.color.text_primary));
            text1.setTextSize(16);
            
            text2.setText(faq[1]);
            text2.setTextColor(getResources().getColor(R.color.text_secondary));
            text2.setVisibility(View.GONE);
            text2.setPadding(0, 8, 0, 16);

            itemView.setOnClickListener(v -> {
                if (text2.getVisibility() == View.GONE) {
                    text2.setVisibility(View.VISIBLE);
                } else {
                    text2.setVisibility(View.GONE);
                }
            });

            faqContainer.addView(itemView);
        }
    }

    private void setupChat() {
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        View btnAttach = findViewById(R.id.btnAttach);

        if (btnAttach != null) {
            btnAttach.setOnClickListener(v -> Toast.makeText(this, "Anexar arquivo (Em breve)", Toast.LENGTH_SHORT).show());
        }

        messages = new ArrayList<>();
        messages.add(new ChatMessage("Olá! Como posso ajudar você hoje?", false));

        chatAdapter = new ChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);
        btnSendMessage.setOnClickListener(v -> sendMessage());
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (!text.isEmpty()) {
            messages.add(new ChatMessage(text, true));
            chatAdapter.notifyItemInserted(messages.size() - 1);
            rvChat.scrollToPosition(messages.size() - 1);
            etMessage.setText("");

            // LLM preparação, aguardando resposta do Bot
            ChatMessage typingMsg = new ChatMessage("Digitando...", false);
            typingMsg.isTyping = true;
            messages.add(typingMsg);
            chatAdapter.notifyItemInserted(messages.size() - 1);
            rvChat.scrollToPosition(messages.size() - 1);

            Content content = new Content.Builder()
                    .addText(text)
                    .build();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    runOnUiThread(() -> {
                        int index = messages.indexOf(typingMsg);
                        if (index != -1) {
                            messages.remove(index);
                            chatAdapter.notifyItemRemoved(index);
                        }
                        String responseText = result.getText();
                        messages.add(new ChatMessage(responseText != null ? responseText : "Desculpe, não consegui processar sua mensagem.", false));
                        chatAdapter.notifyItemInserted(messages.size() - 1);
                        rvChat.scrollToPosition(messages.size() - 1);
                    });
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    runOnUiThread(() -> {
                        int index = messages.indexOf(typingMsg);
                        if (index != -1) {
                            messages.remove(index);
                            chatAdapter.notifyItemRemoved(index);
                        }
                        messages.add(new ChatMessage("Erro ao conectar com a IA: " + t.getMessage(), false));
                        chatAdapter.notifyItemInserted(messages.size() - 1);
                        rvChat.scrollToPosition(messages.size() - 1);
                    });
                }
            }, executor);
        }
    }

    static class ChatMessage {
        String text;
        boolean isUser;
        boolean isTyping;
        ChatMessage(String t, boolean u) { text = t; isUser = u; }
    }

    static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
        List<ChatMessage> data;
        ChatAdapter(List<ChatMessage> d) { data = d; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
            View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_chat_message, p, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int i) {
            ChatMessage m = data.get(i);
            h.text.setText(m.text);
            
            if (m.isTyping) {
                h.text.setAlpha(0.6f);
                h.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            } else {
                h.text.setAlpha(1.0f);
                h.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            }

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) h.bubble.getLayoutParams();
            if (m.isUser) {
                ((LinearLayout) h.container).setGravity(android.view.Gravity.END);
                h.bubble.setBackgroundResource(R.drawable.bg_chat_bubble_user);
                h.text.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.white));
            } else {
                ((LinearLayout) h.container).setGravity(android.view.Gravity.START);
                h.bubble.setBackgroundResource(R.drawable.bg_chat_bubble_support);
                h.text.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.text_primary));
            }
            h.bubble.setLayoutParams(lp);
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView text;
            LinearLayout bubble, container;
            VH(View v) { 
                super(v); 
                text = v.findViewById(R.id.tvMessage);
                bubble = v.findViewById(R.id.msgBubble);
                container = v.findViewById(R.id.msgContainer);
            }
        }
    }

    private void updateThemeIcon(ImageView btn) {
        if (btn == null) return;
        int mode = AppCompatDelegate.getDefaultNightMode();
        if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            btn.setImageResource(R.drawable.ic_sun);
        } else {
            btn.setImageResource(R.drawable.ic_moon);
        }
    }
}
