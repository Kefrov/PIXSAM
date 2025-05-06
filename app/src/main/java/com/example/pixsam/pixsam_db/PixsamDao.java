package com.example.pixsam.pixsam_db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PixsamDao {
    @Insert
    long insertDrawing(DrawingItem drawing);

    @Delete
    void deleteDrawing(DrawingItem drawing);

    @Update
    void updateDrawing(DrawingItem drawing);

    @Query("SELECT * FROM drawings")
    List<DrawingItem> getAllDrawings();

    @Query("SELECT * FROM drawings WHERE drawing_id = :drawingId LIMIT 1")
    DrawingItem getDrawingById(long drawingId);
    @Insert
    void insertColoredPixel(ColoredPixel pixel);

    @Query("SELECT * FROM pixels WHERE drawingId = :drawingId")
    List<ColoredPixel> getPixelsForDrawing(long drawingId);

    @Query("DELETE FROM pixels WHERE drawingId = :drawingId")
    void deletePixelsForDrawing(int drawingId);
}
