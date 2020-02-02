package com.example.cheaptrip.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.cheaptrip.dao.database.VehicleDatabaseClient;
import com.example.cheaptrip.models.fueleconomy.VehicleDataSet;


/**
 * Database holding all the informations of vehicles
 * This is done by using Library Room.
 *
 * (see https://developer.android.com/topic/libraries/architecture/room)
 */
@Database(entities = {VehicleDataSet.class}, version = 2,exportSchema = false)
public abstract class VehicleDatabase extends RoomDatabase {

    public abstract VehicleDatabaseClient vehicleDatabaseClient();
    private static VehicleDatabase INSTANCE;

    public static VehicleDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (VehicleDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE =
                            Room.databaseBuilder(context.getApplicationContext(),VehicleDatabase.class, "VEHICLES")
                                    .allowMainThreadQueries()
                                    .fallbackToDestructiveMigration()
                                    .build();
                }
            }
        }
        return INSTANCE;
    }
}