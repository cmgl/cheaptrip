package com.example.cheaptrip.services;

import org.junit.Test;

import static org.junit.Assert.*;

public class RouteServiceTest {
    // TODO: remove commented block
    /*
    @Test
    public void test() {
        // arrange
        double distance = 500;
        double pricePerLiter = 1.30;
        double avgConsumption = 20;
        Double expectedResult = 130.0;

        // act
        Double actualResult = RouteService.calculateCostsForRoute(distance, pricePerLiter, avgConsumption);

        // assert
        //assertEquals(expectedResult, actualResult);
        System.out.println(actualResult);
    }
    */

    @Test
    public void interpolateConsumption() {
        // arrange
        double avgSpeed = 90;
        double cityMPG = 20;
        double highwayMPG = 22;
        Double expectedResult = 21.6;

        // act
        Double actualResult = RouteService.interpolateConsumption(avgSpeed, cityMPG, highwayMPG);

        // assert
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void calculateCostsForRoute() {
        // arrange
        double distance = 500;
        double pricePerLiter = 1.30;
        double avgConsumption = 20;
        Double expectedResult = 130.0;

        // act
        Double actualResult = RouteService.calculateCostsForRoute(distance, pricePerLiter, avgConsumption);

        // assert
        assertEquals(expectedResult, actualResult);
    }
}