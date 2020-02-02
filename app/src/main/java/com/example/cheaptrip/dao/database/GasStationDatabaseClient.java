package com.example.cheaptrip.dao.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cheaptrip.models.tankerkoenig.Station;

import java.util.List;

@Dao
public interface GasStationDatabaseClient {

    @Query("SELECT * FROM STATIONS WHERE ID = :uuid LIMIT 1")
    Station getForID(String uuid);
    /*=========================================================================================
     * Other Queries
     *=========================================================================================*/
    @Query("SELECT * FROM STATIONS")
    List<Station> getAll();

    @Insert
    void insertAll(List<Station> stations);

    @Update
    void updateAll (List<Station> stations);

    @Delete
    void delete(Station station);

    @Query("DELETE FROM STATIONS")
    void deleteAll();
}
