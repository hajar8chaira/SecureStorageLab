package com.example.securestoragelabjava.ui;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.securestoragelabjava.R;
import com.example.securestoragelabjava.cache.CacheStore;
import com.example.securestoragelabjava.files.InternalTextStore;
import com.example.securestoragelabjava.files.StudentsJsonStore;
import com.example.securestoragelabjava.model.Student;
import com.example.securestoragelabjava.prefs.AppPrefs;
import com.example.securestoragelabjava.prefs.SecurePrefs;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final List<String> langs = Arrays.asList("fr", "en", "ar");

    private EditText etName, etToken;
    private Spinner spLang;
    private Switch swDark;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etToken = findViewById(R.id.etToken);
        spLang = findViewById(R.id.spLang);
        swDark = findViewById(R.id.swDark);
        tvResult = findViewById(R.id.tvResult);

        setupLangSpinner();

        findViewById(R.id.btnSavePrefs).setOnClickListener(v -> savePrefs());
        findViewById(R.id.btnLoadPrefs).setOnClickListener(v -> loadPrefsToUi());
        findViewById(R.id.btnSaveJson).setOnClickListener(v -> saveJsonFile());
        findViewById(R.id.btnLoadJson).setOnClickListener(v -> loadJsonFile());
        findViewById(R.id.btnClear).setOnClickListener(v -> clearAll());

        loadPrefsToUi();
    }

    private void setupLangSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, langs);
        spLang.setAdapter(adapter);
    }

    private void savePrefs() {
        String name = etName.getText().toString().trim();
        String lang = langs.get(Math.max(0, spLang.getSelectedItemPosition()));
        String theme = swDark.isChecked() ? "dark" : "light";

        AppPrefs.save(this, name, lang, theme, false);

        String token = etToken.getText().toString();
        if (!token.isBlank()) {
            try { SecurePrefs.saveToken(this, token); } catch (Exception e) {
                tvResult.setText("Erreur chiffrement token : " + e.getMessage());
                return;
            }
        }

        try { CacheStore.write(this, "last_ui.txt", "name=" + name + ", lang=" + lang + ", theme=" + theme); }
        catch (Exception ignored) {}

        tvResult.setText("Sauvegarde prefs terminée.\nname=" + name + "\nlang=" + lang + "\ntheme=" + theme);
    }

    private void loadPrefsToUi() {
        AppPrefs.Triple triple = AppPrefs.load(this);
        etName.setText(triple.name);
        swDark.setChecked("dark".equals(triple.theme));
        spLang.setSelection(langs.indexOf(triple.lang));

        int tokenLen = 0;
        try { String token = SecurePrefs.loadToken(this); tokenLen = token != null ? token.length() : 0; }
        catch (Exception ignored) {}

        tvResult.setText("Chargement prefs terminé.\nname=" + triple.name + "\nlang=" + triple.lang +
                "\ntheme=" + triple.theme + "\ntokenLength=" + tokenLen);
    }

    private void saveJsonFile() {
        List<Student> students = Arrays.asList(
                new Student(1, "Amina", 20),
                new Student(2, "Omar", 21),
                new Student(3, "Sara", 19)
        );

        try {
            StudentsJsonStore.save(this, students);
            InternalTextStore.writeUtf8(this, "note.txt", "Sauvegarde JSON effectuée (UTF-8).");
        } catch (Exception e) {
            tvResult.setText("Erreur sauvegarde JSON : " + e.getMessage());
            return;
        }

        tvResult.setText("Sauvegarde fichier JSON terminée. students=" + students.size());
    }

    private void loadJsonFile() {
        List<Student> students = StudentsJsonStore.load(this);
        String note;
        try { note = InternalTextStore.readUtf8(this, "note.txt"); } catch (Exception e) { note = "(note.txt absent)"; }

        StringBuilder sb = new StringBuilder();
        sb.append("Chargement fichier JSON terminé.\nnote=").append(note).append("\nstudents=").append(students.size()).append("\n");
        for (Student s : students)
            sb.append(" - id=").append(s.id).append(", name=").append(s.name).append(", age=").append(s.age).append("\n");

        tvResult.setText(sb.toString());
    }

    private void clearAll() {
        AppPrefs.clear(this);
        try { SecurePrefs.clear(this); } catch (Exception ignored) {}
        StudentsJsonStore.delete(this);
        InternalTextStore.delete(this, "note.txt");
        CacheStore.purge(this);

        etName.setText(""); etToken.setText(""); swDark.setChecked(false); spLang.setSelection(0);
        tvResult.setText("Nettoyage terminé.");
    }
}