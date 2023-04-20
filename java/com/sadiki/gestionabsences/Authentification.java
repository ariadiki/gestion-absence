package com.sadiki.gestionabsences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.sadiki.gestionabsences.Firebase.FirebaseHelper;

public class Authentification extends AppCompatActivity {
    TextInputEditText username, password;
    String email, pass;
    Button login;
    FirebaseAuth auth;
    FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentification);
        //instance Firebase
       firebaseHelper = FirebaseHelper.getInstance();
       auth= firebaseHelper.getAuth();

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);

        login.setOnClickListener(v -> {
            //verifier si les champs sont pas vide
            if (!username.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
                /*Firebase ne supporte pas 'username' comme authentification pour ca
                j'ai combiner le avec '@google.com' pour authentifier par email*/
                email = username.getText().toString() + "@google.com";
                pass = password.getText().toString();
                auth.signInWithEmailAndPassword(email, pass)//verifier email et mtp dans bd
                        //si l'operation est complet avec succes
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())//bien authentifier
                                {
                                    username.setText("");
                                    password.setText("");
                                    Intent intent = new Intent(getApplicationContext(),GroupActivity.class);
                                    startActivity(intent);
                                }
                            }
                        })

                        //si l'operation a echouer
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Authentification.this, "Échec de connexion!", Toast.LENGTH_SHORT).show();
                            }
                        });
            //si les champs sont vides
            } else {
                if (username.getText().toString().isEmpty())
                    username.setError("champ requis");
                if (password.getText().toString().isEmpty())
                    password.setError("champ requis");
            }
        });
    }
}