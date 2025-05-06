package com.example.pixsam;

import static java.lang.Float.max;
import static java.lang.Float.min;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pixsam.pixsam_db.ColoredPixel;
import com.example.pixsam.pixsam_db.DrawingItem;
import com.example.pixsam.pixsam_db.PixsamDao;
import com.example.pixsam.pixsam_db.PixsamDatabase;

import java.util.Arrays;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainUIActivity extends AppCompatActivity {

    private int ROWS;
    private int COLUMNS;
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

    private int cellSizePx;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String source = getIntent().getStringExtra("source");

        frameLayout = findViewById(R.id.mainFrame);
        gridLayout = findViewById(R.id.gridLayout);
        float scale = getResources().getDisplayMetrics().density;
        cellSizePx = (int) (CELL_SIZE_DP * scale + 0.5f);

        if (source.equals("plus_button")) {
            ROWS = getIntent().getIntExtra("height", 10);
            COLUMNS = getIntent().getIntExtra("width", 10);
            gridLayout.setRowCount(ROWS);
            gridLayout.setColumnCount(COLUMNS);
            loadEmptyGrid();
        } else if (source.equals("recycler_view_item")) {
            int drawingId = getIntent().getIntExtra("drawing_id", -1);
            new Thread(() -> {
                PixsamDatabase db = PixsamDatabase.getDatabase(this);
                PixsamDao pixsam = db.pixsamDao();
                List<ColoredPixel> coloredPixels = pixsam.getPixelsForDrawing(drawingId);
                DrawingItem drawing = pixsam.getDrawingById(drawingId);

                runOnUiThread(() -> {
                    ROWS = drawing.getHeight();
                    COLUMNS = drawing.getWidth();
                    gridLayout.setRowCount(ROWS);
                    gridLayout.setColumnCount(COLUMNS);

                    int totalWidth = cellSizePx * COLUMNS;
                    int totalHeight = cellSizePx * ROWS;
                    gridLayout.getLayoutParams().width = totalWidth;
                    gridLayout.getLayoutParams().height = totalHeight;
                    gridLayout.requestLayout();

                    loadDrawingById(coloredPixels);
                });

            }).start();
        }

        int totalWidth = cellSizePx * COLUMNS;
        int totalHeight = cellSizePx * ROWS;

        gridLayout.getLayoutParams().width = totalWidth;
        gridLayout.getLayoutParams().height = totalHeight;
        gridLayout.requestLayout();

        gridLayout.setScaleX(0.5f);
        gridLayout.setScaleY(0.5f);

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
                        touchedCell.setBackgroundColor(Color.WHITE);
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
                public void onCancel(AmbilWarnaDialog dialog) {}
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
                public void onCancel(AmbilWarnaDialog dialog) {}
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
                public void onCancel(AmbilWarnaDialog dialog) {}
            });
            colorPicker.show();
            return true;
        });

        ImageButton save_button = findViewById(R.id.save_button);
        save_button.setOnClickListener(v -> saveDrawing());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "Landscape mode", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "Portrait mode", Toast.LENGTH_SHORT).show();
        }

        // Optional: Refresh layout manually if needed
        gridLayout.requestLayout();
    }

    private void loadDrawingById(List<ColoredPixel> coloredPixels) {
        gridLayout.removeAllViews();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                View cell = new View(this);
                GridLayout.LayoutParams cellParams = new GridLayout.LayoutParams();
                cellParams.width = cellSizePx;
                cellParams.height = cellSizePx;
                cell.setLayoutParams(cellParams);
                cell.setBackgroundColor(Color.WHITE);
                cell.setBackgroundResource(R.drawable.cell_border);
                for (ColoredPixel pixel : coloredPixels) {
                    if (pixel.getX() == row && pixel.getY() == col) {
                        cell.setBackgroundColor(pixel.getColor());
                        break;
                    }
                }
                gridLayout.addView(cell);
            }
        }
    }

    private void loadEmptyGrid() {
        gridLayout.removeAllViews();
        for (int i = 0; i < ROWS * COLUMNS; i++) {
            View cell = new View(this);
            GridLayout.LayoutParams cellParams = new GridLayout.LayoutParams();
            cellParams.width = cellSizePx;
            cellParams.height = cellSizePx;
            cell.setLayoutParams(cellParams);
            cell.setBackgroundColor(Color.WHITE);
            cell.setBackgroundResource(R.drawable.cell_border);
            gridLayout.addView(cell);
        }
    }

    private void saveDrawing() {
        new Thread(() -> {
            PixsamDatabase db = PixsamDatabase.getDatabase(this);
            PixsamDao pixsam = db.pixsamDao();
            DrawingItem drawing = new DrawingItem("New Pixel Art", COLUMNS, ROWS);
            long drawingId = pixsam.insertDrawing(drawing);

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLUMNS; col++) {
                    int index = row * COLUMNS + col;
                    View cell = gridLayout.getChildAt(index);

                    Drawable background = cell.getBackground();
                    if (background instanceof ColorDrawable) {
                        int color = ((ColorDrawable) cell.getBackground()).getColor();
                        ColoredPixel pixel = new ColoredPixel((int) drawingId, row, col, color);
                        pixsam.insertColoredPixel(pixel);
                    }
                }
            }
            runOnUiThread(() ->
                    Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show()
            );
        }).start();
    }

    private View getTouchedCell(float rawX, float rawY) {
        int[] gridLocation = new int[2];
        gridLayout.getLocationOnScreen(gridLocation);

        float scaleX = gridLayout.getScaleX();
        float scaleY = gridLayout.getScaleY();

        float adjustedX = (rawX - gridLocation[0]) / scaleX;
        float adjustedY = (rawY - gridLocation[1]) / scaleY;

        int col = (int) (adjustedX / cellSizePx);
        int row = (int) (adjustedY / cellSizePx);

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
