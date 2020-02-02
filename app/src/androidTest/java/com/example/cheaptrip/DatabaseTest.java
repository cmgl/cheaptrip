package com.example.cheaptrip;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.cheaptrip.dao.database.VehicleDatabaseClient;
import com.example.cheaptrip.database.VehicleDatabase;
import com.example.cheaptrip.models.fueleconomy.VehicleDataSet;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    @Test
    public void testVehicleDatabase(){
        // arrange
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        VehicleDatabase db = VehicleDatabase.getDatabase(appContext);
        VehicleDatabaseClient client = db.vehicleDatabaseClient();

        List<VehicleDataSet> list =  new ArrayList<>();
        VehicleDataSet vehicle  = new VehicleDataSet(
                "2020",
                "Mercedes-Benz",
                "C220",
                "e5",
                12.0,
                13.0,
                8.0
        );
        list.add(vehicle);

        //act
        client.insertAll(list);
        List<VehicleDataSet> listResult = client.getAll();
        int listResultSize = listResult.size();
        VehicleDataSet vehicleExpected = list.get(0);
        VehicleDataSet vehicleResult = client.getAll().get(listResultSize-1);

        // assert
        assertEquals(0, vehicleExpected.compareTo(vehicleResult));
    }






















}
