package com.example.projectosa;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.projectosa.state.EstadoApp;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private GoogleSignInClient client;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button buttonLogin = findViewById(R.id.buttonLogin);

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this, options);
        launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e){
                        e.printStackTrace();
                    }
                }
            });
        buttonLogin.setOnClickListener((View view) -> {
            client.signOut().addOnCompleteListener(this, task -> {
                // Sign out completo
                Intent signInIntent = client.getSignInIntent();
                // Inicia a activity de login do Google
                launcher.launch(signInIntent);
            });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){ // Já está autenticado
            // Guardar o nome do utilizador no estado da aplicação
            EstadoApp.setUsername(user.getDisplayName());
            // Direcionar o utilizador para a próxima activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // para fechar a atividade de login e impedir que o utilizador volte atrás com o botão de retorno
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    AuthResult result = task.getResult();
                    EstadoApp.setUsername(result.getUser().getDisplayName());
                    // Direcionar o utilizador para a próxima activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // para fechar a atividade de login e impedir que o utilizador volte atrás com o botão de retorno
                } else {
                    // Autenticação falhou
                    assert task.getException() != null; // evita warnings
                    assert task.getException().getMessage() != null; // evita warnings
                    Log.e("DEBUG", task.getException().getMessage());
                    Toast.makeText(LoginActivity.this, "A autenticação falhou", Toast.LENGTH_SHORT).show();
                }
            });
    }

}