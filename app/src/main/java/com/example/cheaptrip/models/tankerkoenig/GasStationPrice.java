package com.example.cheaptrip.models.tankerkoenig;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * These Class contains all prices for specific Gastation
 * on a specific date.
 */
@Entity(foreignKeys = @ForeignKey(entity = Station.class,
        parentColumns = "id",
        childColumns = "stationID",
        onDelete = CASCADE))
public class GasStationPrice
{
    @PrimaryKey
    private int id;

    @ColumnInfo(name="date")
    private String date;

    @ColumnInfo(name="e5")
    private double e5;

    @ColumnInfo(name="e10")
    private double e10;

    @ColumnInfo(name="diesel")
    private double diesel;


    public GasStationPrice(int id, String date, double e5, double e10, double diesel,final int stationID) {
        this.id = id;
        this.date = date;
        this.e5 = e5;
        this.e10 = e10;
        this.diesel = diesel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getE5() {
        return e5;
    }

    public void setE5(double e5) {
        this.e5 = e5;
    }

    public double getE10() {
        return e10;
    }

    public void setE10(double e10) {
        this.e10 = e10;
    }

    public double getDiesel() {
        return diesel;
    }

    public void setDiesel(double diesel) {
        this.diesel = diesel;
    }
}
