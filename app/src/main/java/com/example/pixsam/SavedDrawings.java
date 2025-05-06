package com.example.pixsam;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixsam.pixsam_db.ColoredPixel;
import com.example.pixsam.pixsam_db.DrawingItem;
import com.example.pixsam.pixsam_db.PixsamDao;
import com.example.pixsam.pixsam_db.PixsamDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Objects;

public class SavedDrawings extends AppCompatActivity {
    RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.saveddrawings);
        loadDrawings();
        FloatingActionButton fab = findViewById(R.id.addDrawing);
        fab.setOnClickListener(v -> showGridSizeDialog());

        EditText searchText = findViewById(R.id.search_txt);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String newText = editable.toString();
                if (null != adapter) {
                    adapter.filterList(newText);
                }
            }
        });
    }
    protected void onResume() {
        super.onResume();
        loadDrawings();
    }
    private void loadDrawings() {
        new Thread(() -> {
            PixsamDatabase db = PixsamDatabase.getDatabase(this);
            PixsamDao pixsam = db.pixsamDao();
            List<DrawingItem> drawings = pixsam.getAllDrawings();
            runOnUiThread(() -> {
                if (adapter == null) {
                    initRecyclerView(drawings);
                } else {
                    adapter.updateList(drawings);
                }
            });
        }).start();
    }

    private void showGridSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_grid_size, null);
        builder.setView(view);
        EditText widthEditText = view.findViewById(R.id.widthEditText);
        EditText heightEditText = view.findViewById(R.id.heightEditText);
        Button btnOk = view.findViewById(R.id.btn_ok1);
        Button btnCancel = view.findViewById(R.id.btn_cancel1);

        AlertDialog dialog = builder.create();
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnOk.setOnClickListener(v -> {
            String widthStr = widthEditText.getText().toString().trim();
            String heightStr = heightEditText.getText().toString().trim();

            if (widthStr.isEmpty() || heightStr.isEmpty()) {
                Toast.makeText(this, "Please enter both width and height", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int width = Integer.parseInt(widthStr);
                int height = Integer.parseInt(heightStr);
                if (width < 1 || width > 80 || height < 1 || height > 80) {
                    Toast.makeText(this, "Width and height must be between 1 and 80", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, MainUIActivity.class);
                intent.putExtra("width", width);
                intent.putExtra("height", height);
                intent.putExtra("source", "plus_button");
                startActivity(intent);
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid input. Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }
    private void initRecyclerView(List<DrawingItem> drawinglist) {
        RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(SavedDrawings.this, drawinglist);
        recyclerView.setAdapter(adapter);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null) {
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                float x = event.getRawX();
                float y = event.getRawY();
                if (x < location[0] || x > location[0] + view.getWidth()
                        || y < location[1] || y > location[1] + view.getHeight()) {
                    // Clear focus
                    view.clearFocus();
                    // Hide keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }}}}
        return super.dispatchTouchEvent(event);
    }

}

