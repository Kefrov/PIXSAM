package com.example.pixsam.pixsam_db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DrawingItem.class, ColoredPixel.class}, version = 1, exportSchema = false)
public abstract class PixsamDatabase extends RoomDatabase {
    public abstract PixsamDao pixsamDao(); // access to the DAO methods
    private static PixsamDatabase INSTANCE;
    public static synchronized PixsamDatabase getDatabase(Context context) {
        //context.deleteDatabase("Pixsam_database");
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PixsamDatabase.class, "Pixsam_database")
                    .build();
        }
        return INSTANCE;
    }
}