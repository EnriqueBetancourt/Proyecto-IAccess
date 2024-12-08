package com.example.iacccess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    Button btnInicioSesion;
    ImageButton btnRegresar;
    EditText txtCorreo, txtContrasenia;
    CheckBox cbVerContrasenia;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        txtCorreo = findViewById(R.id.txtCorreo);
        txtContrasenia = findViewById(R.id.txtContrasenia);
        btnInicioSesion = findViewById(R.id.btnCompletarPerfil);
        cbVerContrasenia = findViewById(R.id.cbVerContrasenia);
        btnRegresar = findViewById(R.id.btnRegresar);

        btnInicioSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo, contrasenia;
                correo = String.valueOf(txtCorreo.getText());
                contrasenia = String.valueOf(txtContrasenia.getText());
                if (TextUtils.isEmpty(correo)){
                    Toast.makeText(Login.this, "Ingresa tu correo", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(contrasenia)){
                    Toast.makeText(Login.this, "Ingresa tu contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    mAuth.signInWithEmailAndPassword(correo, contrasenia)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Login.this, "Bienvenido.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Login.this, MenuPrincipal.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(Login.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } catch (Exception e) {
                    Toast.makeText(Login.this, "Error crítico: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        cbVerContrasenia.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                txtContrasenia.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                txtContrasenia.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            txtContrasenia.setSelection(txtContrasenia.getText().length());
        });
    }
}