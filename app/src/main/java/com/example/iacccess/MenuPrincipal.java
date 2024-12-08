package com.example.iacccess;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.iacccess.databinding.ActivityMenuBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class MenuPrincipal extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMenuBinding binding;

    private String currentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Recuperar el idioma guardado en las preferencias
        SharedPreferences preferences = getSharedPreferences("Idioma", MODE_PRIVATE);
        currentLanguage = preferences.getString("lenguaje", "es"); // Por defecto: español

        // Aplicar el idioma antes de inflar el diseño
        applySavedLocale(currentLanguage);

        super.onCreate(savedInstanceState); // Mover super.onCreate después de la configuración del idioma

        // Inflar el diseño
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMenu.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Configurar el Navigation Drawer con el NavController
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.menu_residente, R.id.menuPortero2, R.id.nav_slideshow, R.id.nav_gallery)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_menu);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Manejo de "Cerrar Sesión"
            if (id == R.id.nav_salir) { // Asegúrate de que este sea el ID del ítem "Cerrar Sesión"
                FirebaseAuth.getInstance().signOut(); // Cerrar sesión de Firebase

                // Redirigir al LoginActivity
                Intent intent = new Intent(MenuPrincipal.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }

            // Manejo de navegación
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);

            // Cerrar el drawer si se selecciona una opción válida
            if (handled) {
                drawer.closeDrawer(GravityCompat.START);
            }
            return handled;
        });

        // Escucha para cerrar el drawer al cambiar el destino
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_lenguaje) { // ID único del ítem del menú
            // Alternar entre inglés y español
            String newLanguage = currentLanguage.equals("en") ? "es" : "en";
            changeLanguage(newLanguage);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void applySavedLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void changeLanguage(String languageCode) {
        // Guardar el idioma en SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("Idioma", MODE_PRIVATE).edit();
        editor.putString("lenguaje", languageCode);
        editor.apply();

        // Reiniciar la actividad usando un Intent
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_menu);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
