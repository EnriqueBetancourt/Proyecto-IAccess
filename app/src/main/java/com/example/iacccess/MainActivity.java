package com.example.iacccess;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button btnPantallaRegistrarse;
    private Button btnPantallaLogin;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPantallaLogin = findViewById(R.id.btnPantallaLogin);
        btnPantallaRegistrarse = findViewById(R.id.btnPantallaRegistrarse);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Comprobar si hay un usuario autenticado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Usuario autenticado, redirigir al menú
            Intent intent = new Intent(MainActivity.this, MenuPrincipal.class);
            startActivity(intent);
            finish(); // Finalizar la actividad para que no se pueda volver con "atrás"
        }
    }

    public void pantallaRegistro(View v){
        Intent intent = new Intent(this, Registrarse.class);
        startActivity(intent);
    }

    public void pantallaInicioSesion(View v){
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

}