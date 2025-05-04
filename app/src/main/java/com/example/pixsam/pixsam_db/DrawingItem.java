package com.example.pixsam.pixsam_db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "drawings")
public class DrawingItem {
    @PrimaryKey(autoGenerate = true)
    private int drawing_id;
    private int width;
    private int height;
    private String drawing_name;

    public DrawingItem(String drawing_name, int width, int height) {
        this.drawing_name = drawing_name;
        this.width = width;
        this.height = height;
    }

    public int getDrawing_id() {
        return drawing_id;
    }

    public String getDrawing_name() {
        return drawing_name;
    }
    public void setDrawing_id(int drawing_id) {
        this.drawing_id = drawing_id;
    }
    public void setDrawing_name(String drawing_name) {
        this.drawing_name = drawing_name;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
