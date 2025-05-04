package com.example.pixsam;

import static java.lang.Float.max;
import static java.lang.Float.min;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainUIActivity extends AppCompatActivity {

    private static final int ROWS = 80;
    private static final int COLUMNS = 80;
    private static final int CELL_SIZE_DP = 5;

    private float lastX = 0, lastY = 0;
    private GridLayout gridLayout;
    private FrameLayout frameLayout;

    private String active_button = "Move grid";
    private int activeColor = Color.rgb(255, 0, 0);

    private int color1 = Color.rgb(255, 0, 0);
    private int color2 = Color.rgb(0, 255, 0);
    private int color3 = Color.rgb(0, 0, 255);

    private int selectedColor = Color.RED;

    private View lastTouchedCell = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        float scale = getResources().getDisplayMetrics().density;
        int cellSizePx = (int) (CELL_SIZE_DP * scale + 0.5f);

        frameLayout = findViewById(R.id.mainFrame);
        gridLayout = findViewById(R.id.gridLayout);

        int totalWidth = cellSizePx * COLUMNS;
        int totalHeight = cellSizePx * ROWS;

        gridLayout.getLayoutParams().width = totalWidth;
        gridLayout.getLayoutParams().height = totalHeight;
        gridLayout.requestLayout();

        gridLayout.setScaleX(0.5f);
        gridLayout.setScaleY(0.5f);

        gridLayout.removeAllViews();
        for (int i = 0; i < ROWS * COLUMNS; i++) {
            View cell = new View(this);
            GridLayout.LayoutParams cellParams = new GridLayout.LayoutParams();
            cellParams.width = cellSizePx;
            cellParams.height = cellSizePx;
            cell.setLayoutParams(cellParams);
            cell.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
            cell.setBackgroundResource(R.drawable.cell_border);
            gridLayout.addView(cell);
        }

        ImageButton zoomIn = findViewById(R.id.zin);
        ImageButton zoomOut = findViewById(R.id.zout);

        zoomIn.setOnClickListener(v -> zoomGrid(1.2f));
        zoomOut.setOnClickListener(v -> zoomGrid(0.8f));

        ImageButton color1Button = findViewById(R.id.color1);
        ImageButton color2Button = findViewById(R.id.color2);
        ImageButton color3Button = findViewById(R.id.color3);
        ImageButton moveBtn = findViewById(R.id.move);
        ImageButton eraseBtn = findViewById(R.id.eraser);

        color1Button.getBackground().setColorFilter(color1, PorterDuff.Mode.SRC_ATOP);
        color2Button.getBackground().setColorFilter(color2, PorterDuff.Mode.SRC_ATOP);
        color3Button.getBackground().setColorFilter(color3, PorterDuff.Mode.SRC_ATOP);

        List<ImageButton> toolButtons = Arrays.asList(color1Button, color2Button, color3Button, moveBtn, eraseBtn);

        View.OnClickListener toolClickListener = view -> {
            for (ImageButton button : toolButtons) {
                button.setAlpha(1.0f);
            }
            view.setAlpha(0.5f);
            active_button = view.getContentDescription().toString();

            if (active_button.equals("Color 1")) {
                activeColor = color1;
            } else if (active_button.equals("Color 2")) {
                activeColor = color2;
            } else if (active_button.equals("Color 3")) {
                activeColor = color3;
            }
        };

        for (ImageButton button : toolButtons) {
            button.setOnClickListener(toolClickListener);
        }

        moveBtn.performClick();

        frameLayout.setOnTouchListener((v, event) -> {
            float x = event.getRawX();
            float y = event.getRawY();

            if (active_button.equals("Move grid")) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = x;
                        lastY = y;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = x - lastX;
                        float dy = y - lastY;
                        gridLayout.setTranslationX(gridLayout.getTranslationX() + dx);
                        gridLayout.setTranslationY(gridLayout.getTranslationY() + dy);
                        lastX = x;
                        lastY = y;
                        return true;
                }
            } else if (active_button.equals("Erase pixel") || active_button.equals("Color 1") || active_button.equals("Color 2") || active_button.equals("Color 3")) {
                View touchedCell = getTouchedCell(x, y);
                if (touchedCell != null && touchedCell != lastTouchedCell) {
                    lastTouchedCell = touchedCell;
                    if (active_button.equals("Erase pixel")) {
                        touchedCell.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                        touchedCell.setBackgroundResource(R.drawable.cell_border);
                    } else {
                        touchedCell.setBackgroundColor(activeColor);
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    lastTouchedCell = null;
                }
                return true;
            }

            return false;
        });

        color1Button.setOnLongClickListener(view -> {
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(MainUIActivity.this, selectedColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    selectedColor = color;
                    color1 = color;
                    color1Button.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    color1Button.performClick();
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // User clicked cancel
                }
            });
            colorPicker.show();

            return true;
        });

        color2Button.setOnLongClickListener(view -> {
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(MainUIActivity.this, selectedColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    selectedColor = color;
                    color2 = color;
                    color2Button.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    color2Button.performClick();
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // User clicked cancel
                }
            });
            colorPicker.show();

            return true;
        });

        color3Button.setOnLongClickListener(view -> {
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(MainUIActivity.this, selectedColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    selectedColor = color;
                    color3 = color;
                    color3Button.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    color3Button.performClick();
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // User clicked cancel
                }
            });
            colorPicker.show();

            return true;
        });
    }

    private View getTouchedCell(float rawX, float rawY) {
        int[] gridLocation = new int[2];
        gridLayout.getLocationOnScreen(gridLocation);

        float scaleX = gridLayout.getScaleX();
        float scaleY = gridLayout.getScaleY();

        float adjustedX = (rawX - gridLocation[0]) / scaleX;
        float adjustedY = (rawY - gridLocation[1]) / scaleY;

        float cellSizePx = CELL_SIZE_DP * getResources().getDisplayMetrics().density;

        int col = (int) (adjustedX / cellSizePx - 0.5f);
        int row = (int) (adjustedY / cellSizePx - 0.5f);

        if (row >= 0 && row < ROWS && col >= 0 && col < COLUMNS) {
            int index = row * COLUMNS + col;
            return gridLayout.getChildAt(index);
        }
        return null;
    }

    private void zoomGrid(float factor) {
        float currentScaleX = gridLayout.getScaleX();
        float currentScaleY = gridLayout.getScaleY();
        gridLayout.setScaleX(min(max(currentScaleX * factor, 0.5f), 12f));
        gridLayout.setScaleY(min(max(currentScaleY * factor, 0.5f), 12f));
    }
}
