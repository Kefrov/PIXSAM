package com.example.pixsam.pixsam_db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pixels")
public class ColoredPixel {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int drawingId;
    private int x, y;
    private String colorHex;

    // Constructor
    public ColoredPixel(int drawingId, int x, int y, String colorHex) {
        this.drawingId = drawingId;
        this.x = x;
        this.y = y;
        this.colorHex = colorHex;
    }

    // Getter methods
    public int getId() {
        return id;
    }

    public int getDrawingId() {
        return drawingId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getColorHex() {
        return colorHex;
    }

    // Setter methods
    public void setId(int id) {
        this.id = id;
    }

    public void setDrawingId(int drawingId) {
        this.drawingId = drawingId;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
}
