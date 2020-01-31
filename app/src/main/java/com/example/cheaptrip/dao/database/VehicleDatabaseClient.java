package com.example.cheaptrip.dao.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cheaptrip.database.VehicleDatabase;
import com.example.cheaptrip.models.fueleconomy.VehicleDataSet;

import java.util.List;


/**
 * A Data Access Object interface defining function for handling database Queries.
 * Each function describes a Database Access Query, which will be used by Room library.
 * (see https://developer.android.com/topic/libraries/architecture/room)
 */
@Dao
public interface VehicleDatabaseClient {
    /*=========================================================================================
     * Functions getting all Distinct Entries for a specific Column
     *=========================================================================================*/
    @Query("SELECT * FROM VEHICLES")
    List<VehicleDataSet> getAll();

    @Query("SELECT DISTINCT (BRAND) FROM VEHICLES WHERE NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)")
    List<String> getAllBrands();

    @Query("SELECT DISTINCT (MODEL) FROM VEHICLES WHERE NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)")
    List<String> getAllModels();

    @Query("SELECT DISTINCT (YEAR) FROM VEHICLES WHERE NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)")
    List<String> getAllYears();
    /*=========================================================================================
     * Functions getting Column BRAND
     *=========================================================================================*/
    @Query("SELECT DISTINCT(BRAND) FROM VEHICLES WHERE YEAR = :year AND NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)")
    List<String> getBrandsForYear(String year);

    @Query("SELECT DISTINCT(BRAND) FROM VEHICLES WHERE MODEL = :model AND NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)")
    List<String> getBrandsForModel(String model);
    /*=========================================================================================
     * Functions getting Column MODEL
     *=========================================================================================*/
    @Query("SELECT DISTINCT (MODEL) FROM VEHICLES WHERE BRAND = :brand AND NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)")
    List<String> getModelForBrand(String brand);

    @Query("SELECT DISTINCT (MODEL) FROM VEHICLES WHERE BRAND = :brand  AND YEAR = :year AND NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)")
    List<String> getModelForBrandandYear(String brand, String year);

    @Query("SELECT DISTINCT (MODEL) FROM VEHICLES WHERE YEAR = :year AND NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)")
    List<String> getModelForYear(String year);
    /*=========================================================================================
     * Functions getting Column YEAR
     *=========================================================================================*/
    @Query("SELECT DISTINCT (YEAR) FROM VEHICLES WHERE BRAND = :brand AND NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)")
    List<String> getYearsForBrand(String brand);

    @Query("SELECT DISTINCT (YEAR) FROM VEHICLES WHERE BRAND = :brand AND MODEL = :model AND NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)")
    List<String> getYearForBrandAndModel(String brand, String model);

    @Query("SELECT DISTINCT (YEAR) FROM VEHICLES WHERE  MODEL = :model AND NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)")
    List<String> getYearForModel(String model);

    @Query("SELECT DISTINCT (YEAR) FROM VEHICLES WHERE  BRAND = :brand AND NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)")
    List<String> getYearForBrand(String brand);
    /*=========================================================================================
     * Functions getting a full VehicleDataSet
     *=========================================================================================*/
    @Query("SELECT * FROM VEHICLES WHERE BRAND = :brand AND MODEL = :model and YEAR = :year AND NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)  LIMIT 1")
    VehicleDataSet findVehicle(String brand, String model,String year);

    @Query("SELECT * FROM VEHICLES WHERE YEAR = :year AND NOT(PREMIUM is null AND REGULAR is null AND DIESEL is null)")
    List<VehicleDataSet> getVechiclesForYear(String year);
    /*=========================================================================================
     * Other Queries
     *=========================================================================================*/
    @Insert
    void insertAll(List<VehicleDataSet> vehicleDataSets);

    @Update
    void updateAll (List<VehicleDataSet> vehicleDatabases);

    @Delete
    void delete(VehicleDataSet user);

    @Query("DELETE FROM VEHICLES")
    void deleteAll();
}
