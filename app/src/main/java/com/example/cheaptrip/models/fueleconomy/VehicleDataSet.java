package com.example.cheaptrip.models.fueleconomy;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "VEHICLES")
public class VehicleDataSet  implements Comparable<VehicleDataSet>{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "UID")
    private long vehicleID;

    @ColumnInfo(name = "YEAR")
    private String constructionYear;

    @ColumnInfo(name = "BRAND")
    private String vehicleBrand;

    @ColumnInfo(name = "MODEL")
    private String vehicleModel;

    @ColumnInfo(name = "FUELTYPE")
    private String fuelType;

    @ColumnInfo(name = "CITY")
    private Double consumptionCity;

    @ColumnInfo(name = "HIGHWAY")
    private Double consumptionHighway;


    @ColumnInfo(name = "COMBINED")
    private Double consumptionCombined;

    public VehicleDataSet(String constructionYear,
                          String vehicleBrand,
                          String vehicleModel,
                          String fuelType,
                          Double consumptionCity,
                          Double consumptionHighway,
                          Double consumptionCombined)
    {
        this.constructionYear = constructionYear;
        this.vehicleBrand = vehicleBrand;
        this.vehicleModel = vehicleModel;
        this.fuelType = fuelType;
        this.consumptionCity = consumptionCity;
        this.consumptionHighway = consumptionHighway;
        this.consumptionCombined = consumptionCombined;
    }

    public String getConstructionYear() {
        return constructionYear;
    }

    public void setConstructionYear(String constructionYear) {
        this.constructionYear = constructionYear;
    }

    public String getVehicleBrand() {
        return vehicleBrand;
    }

    public void setVehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public Double getConsumptionCity() {
        return consumptionCity;
    }

    public Double getConsumptionHighway() {
        return consumptionHighway;
    }

    public Double getConsumptionCombined() {
        return consumptionCombined;
    }

    public void setConsumptionCity(Double consumptionCity) {
        this.consumptionCity = consumptionCity;
    }

    public void setConsumptionHighway(Double consumptionHighway) {
        this.consumptionHighway = consumptionHighway;
    }

    public void setConsumptionCombined(Double consumptionCombined) {
        this.consumptionCombined = consumptionCombined;
    }


    public long getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(long vehicleID) {
        this.vehicleID = vehicleID;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }


    @Override
    public int compareTo(VehicleDataSet vehicleDataSet) {
        if(vehicleDataSet == null){
            return 1;
        }

        String thisDataString =
                getConstructionYear() + getVehicleBrand() + getVehicleModel() +
                        getConsumptionCombined() + getConsumptionHighway() + getConsumptionCity();

        String dataString2 =
                vehicleDataSet.getConstructionYear() + vehicleDataSet.getVehicleBrand() + vehicleDataSet.getVehicleModel() +
                        vehicleDataSet.getConsumptionCombined() + vehicleDataSet.getConsumptionHighway() + vehicleDataSet.getConsumptionCity();


        return thisDataString.compareTo(dataString2);

    }


}
