package com.example.cheaptrip.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.cheaptrip.activities.CalculationActivity;
import com.example.cheaptrip.dao.database.VehicleDatabaseClient;
import com.example.cheaptrip.dao.rest.GasStationClient;
import com.example.cheaptrip.database.VehicleDatabase;
import com.example.cheaptrip.handlers.CalculationListener;
import com.example.cheaptrip.handlers.rest.geo.GeoDirectionMatrixHandler;
import com.example.cheaptrip.handlers.rest.geo.GeoDirectionsHandler;
import com.example.cheaptrip.handlers.rest.vehicle.VehiclePropertyHandler;

import com.example.cheaptrip.handlers.rest.station.GasStationForRadiusHandler;
import com.example.cheaptrip.handlers.rest.station.GasStationHistoryHandler;
import com.example.cheaptrip.handlers.rest.station.GasStationHistoryPriceHandler;
import com.example.cheaptrip.models.TripGasStation;
import com.example.cheaptrip.models.TripLocation;
import com.example.cheaptrip.models.TripRoute;
import com.example.cheaptrip.models.TripVehicle;

import com.example.cheaptrip.models.fueleconomy.Vehicles;
import com.example.cheaptrip.models.tankerkoenig.Station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;



public class RouteService extends AsyncTask<TripLocation,Void,Void> {

    final static double RADIUS_EARTH = 6371e3;    // Radius of Earth in meters

    final static double GAS_STATION_SEARCH_RADIUS = 25; // Radius to search for Gas-Stations
    // 25 max allowed by tankerkoenig

    private TripVehicle tripVehicle;
    List<TripRoute> tripRouteList;

    private Context context;

    CalculationListener calculationListener;    // for Callbacks

    public RouteService(Context context, TripVehicle tripVehicle, CalculationListener calculationListener){
        assert(tripVehicle != null);

        this.tripVehicle = tripVehicle;
        this.context = context;
        tripRouteList = new ArrayList<>();
        this.calculationListener = calculationListener;
    }

    @Override
    protected Void doInBackground(TripLocation... tripLocations) {

        if (tripLocations.length < 2){
            Log.e("CHEAPTRIP","locations must be greater than 2 (start + destination)");
            return null;
        }

        TripLocation startTripLocation = tripLocations[0];
        TripLocation endTripLocation = tripLocations[1];

        if (startTripLocation == null){
            Log.e("CHEAPTRIP","Start location is empty.");
            return null;
        }

        if (endTripLocation == null){
            Log.e("CHEAPTRIP","End location is empty.");
            return null;
        }
        /*============================================================================================
         * Initialize the TripVehicle (set its Properties from Rest-API, e.g. Consumption)
         *============================================================================================*/
        tripVehicle = initTripVehicle(tripVehicle);

        if (tripVehicle == null){
            return null;
        }
        /*==============================================================================================
         * Determine Route from Start to End Location
         *==============================================================================================*/
        GeoDirectionsHandler geoDirectionsHandler = new GeoDirectionsHandler(Arrays.asList(tripLocations),null);
        TripRoute tripRoute = geoDirectionsHandler.makeSyncRequest();

        if(tripRoute == null){
            Log.e("CHEAPTRIP","Error on calling OpenrouteService: TripLocation is null");
            return null;
        }
        /*============================================================================================
         * Get the distance the user will get with the Consumption and and tank capacity
         *============================================================================================*/
        double maxRange = determineMaxRange(tripVehicle, tripRoute);
        /*============================================================================================
         * Get the Points describing the PolyLine (to be drawn)
         * This is just a connection of points via linear lines
         *============================================================================================*/
        List<TripLocation> tripLocationList = tripRoute.getPointsForPolyLine();
        /*============================================================================================
         * Determine the Point on the Route from where to find Gas Stations nearby
         *============================================================================================*/
        TripLocation pointInRange = findPointfromDistance(maxRange,tripLocationList,GAS_STATION_SEARCH_RADIUS);
        /*============================================================================================
         * Determine the Point on the Route from where to find Gas Stations nearby
         *============================================================================================*/
        List<TripGasStation> stationList = getStationsInRange(pointInRange);

        tripRouteList = determineTripRoutes(startTripLocation,endTripLocation,stationList,tripVehicle);

        return null;
    }

    private TripRoute recursiveCalculation(TripVehicle tripVehicle ,TripLocation ... tripLocations){
        /*==========================================================================================
         * Sanity Checks
         *=========================================================================================*/

        if (tripVehicle == null){
            return null;
        }

        if(tripLocations == null || tripLocations.length < 2){
            Log.e("CHEAPTRIP","RouteService: Cannot determine Route: Start and end location not set");
            return null;
        }

        // Get the last 2 TripLocations of the route to calculate
        TripLocation startTripLocation = tripLocations[tripLocations.length-2];
        TripLocation endTripLocation = tripLocations[tripLocations.length-1];
        /*==============================================================================================
         * Determine Route from Start to End Location
         *==============================================================================================*/
        GeoDirectionsHandler geoDirectionsHandler = new GeoDirectionsHandler(Arrays.asList(tripLocations),null);
        TripRoute nextTripRoute = geoDirectionsHandler.makeSyncRequest();

        if(nextTripRoute == null){
            Log.e("CHEAPTRIP","Error on calling OpenrouteService: TripLocation is null");
            return null;
        }
        /*============================================================================================
         * compare
         * -------------
         *      1) the distance the user will get with the Consumption and and tank capacity
         *      2) the distance between the startPoint and the Endpoint
         * if
         * -------------
         *      1) distance < maxRange: The user will get to the Destination without refuel
         *      2) else:                The user has to refuel
         *                              ->  Calculate the route from the next station to the destination
         *                                  (recursive call)
         *============================================================================================*/
        double distance = calculateDistance(startTripLocation,endTripLocation);
        double maxRange = determineMaxRange(tripVehicle, nextTripRoute);


        List<List<TripGasStation>> listOfStationLists = new ArrayList<>();

        while(distance > maxRange){
            List<TripLocation> locationsForPolyLine = nextTripRoute.getPointsForPolyLine();
            TripLocation pointInRange = findPointfromDistance(maxRange,locationsForPolyLine,GAS_STATION_SEARCH_RADIUS);

            List<TripGasStation> stationList = getStationsInRange(pointInRange);

            tripRouteList = determineTripRoutes(startTripLocation,endTripLocation,stationList,tripVehicle);

            List<TripRoute> cheapestTripRoutes = getCheapestRoutes(tripRouteList,20);

            List<TripGasStation> cheapestStations = new ArrayList<>();

            for(TripRoute tripRoute : cheapestTripRoutes){

                for(TripLocation tripLocation :tripRoute.getStops()){
                    if (tripLocation instanceof TripGasStation){
                        cheapestStations.add((TripGasStation)tripLocation);
                    }
                }

            }
            listOfStationLists.add(stationList);
        }

        if(distance < maxRange){
            /*============================================================================================
             * Get the Points describing the PolyLine (to be drawn)
             * This is just a connection of points via linear lines
             *============================================================================================*/
            List<TripLocation> locationsForPolyLine = nextTripRoute.getPointsForPolyLine();
            /*============================================================================================
             * Determine the Point on the Route from where to find Gas Stations nearby
             *============================================================================================*/
            TripLocation pointInRange = findPointfromDistance(maxRange,locationsForPolyLine,GAS_STATION_SEARCH_RADIUS);
            /*============================================================================================
             * Determine the Point on the Route from where to find Gas Stations nearby
             *==========================================================================================*/
            List<TripGasStation> stationList = getStationsInRange(pointInRange);

            tripRouteList = determineTripRoutes(startTripLocation,endTripLocation,stationList,tripVehicle);
            return nextTripRoute;
        }
        /*============================================================================================
         * Get the Points describing the PolyLine (to be drawn)
         * This is just a connection of points via linear lines
         *============================================================================================*/
        List<TripLocation> locationsForPolyLine = nextTripRoute.getPointsForPolyLine();
        /*============================================================================================
         * Determine the Point on the Route from where to find Gas Stations nearby
         *============================================================================================*/
        TripLocation pointInRange = findPointfromDistance(maxRange,locationsForPolyLine,GAS_STATION_SEARCH_RADIUS);
        /*============================================================================================
         * Determine the Point on the Route from where to find Gas Stations nearby
         *==========================================================================================*/
        List<TripGasStation> stationList = getStationsInRange(pointInRange);

        tripRouteList = determineTripRoutes(startTripLocation,endTripLocation,stationList,tripVehicle);


        if(tripRouteList == null || tripRouteList.isEmpty()){
            Log.e("CHEAPTRIP","RouteService: Calculation returned empty List or Error");
            return null;
        }

        List<TripRoute> cheapestTripRoutes = getCheapestRoutes(tripRouteList,20);

        for(TripRoute tripRoute :tripRouteList){

        }
        /*=========================================================================================
         * Get a Route to test whether the next Station to peek is in the radius of the destination
         * (it doesen't matter which one)
         * This is just a representation for the other distances between GasStation and
         * Destinations (because the other stations are within the same radius
         *=========================================================================================*/
        TripRoute tripRoutePeek = tripRouteList.get(0);
        List<TripLocation> stopsAmongTheRoute = tripRoutePeek.getStops();

        // Must have at least three stops (Start -> Station ( -> Station -> ...) -> Destination)
        if(stopsAmongTheRoute == null || stopsAmongTheRoute.size() < 3){
            Log.e("CHEAPTRIP","RouteService: Route is null or has less then 3 stops");
            return null;
        }

        // Ensure that the Stop in the middle is a gasStation
        if(!(stopsAmongTheRoute.get(1) instanceof TripGasStation)){
            Log.e("CHEAPTRIP","RouteService: the intermediate TripLocaiton is not a TripGasStation");
            return null;
        }

        TripGasStation tripGasStation = (TripGasStation) stopsAmongTheRoute.get(1);

        // Reset the Tank of the Vehicle to 100% capacity
        TripVehicle vehicleFullTank = new TripVehicle(tripVehicle);
        vehicleFullTank.setRemainFuelPercent(100);

        //recursiveCalculation(tripRoutePeek,vehicleFullTank,tripGasStation,endTripLocation);
        return tripRoutePeek;
    }

    public TripVehicle initTripVehicle(TripVehicle tripVehicle){
        if (tripVehicle == null){
            Log.e("CHEAPTRIP","Cannot assign Properties to tripVehicle: tripVehicle is null");
            return null;
        }
/*
        VehiclePropertyHandler vehiclePropertyHandler = new VehiclePropertyHandler(tripVehicle);

        Vehicles vehicles = vehiclePropertyHandler.makeRequest();

        if( vehicles == null){
            Log.e("CHEAPTRIP","Cannot assign Properties to tripVehicle: Could not get Properties from REST-API");
            return null;
        }

        String strCityMPG = vehicles.getVehicleList().get(0).getCity08();
        double cityMPG = Double.parseDouble(strCityMPG);

        String strHighwayMPG = vehicles.getVehicleList().get(0).getHighway08();
        double highwayMPG = Double.parseDouble(strHighwayMPG);
*/
        VehicleDatabaseClient dbClient = VehicleDatabase.getDatabase(context).vehicleDatabaseClient();


        /*Double cityMPG = dbClient.getConsumptionCity(tripVehicle.getBrand(),tripVehicle.getModel(),tripVehicle.getYear());
        Double highwayMPG = dbClient.getConsumptionHighway(tripVehicle.getBrand(),tripVehicle.getModel(),tripVehicle.getYear());

        double cityKML = convertMPGto100KML(cityMPG);
        double highwayKML = convertMPGto100KML(highwayMPG);*/

        double cityKML    =  dbClient.getConsumptionCity(tripVehicle.getBrand(),tripVehicle.getModel(),tripVehicle.getYear());
        double highwayKML =  dbClient.getConsumptionHighway(tripVehicle.getBrand(),tripVehicle.getModel(),tripVehicle.getYear());

        tripVehicle.setFuelConsumptionHighway(highwayKML);
        tripVehicle.setFuelConsumptionCity(cityKML);

        return tripVehicle;
    }


    public List<TripRoute> determineTripRoutes(TripLocation startLocation, TripLocation endLocation, List<TripGasStation> stationList, TripVehicle tripVehicle){

        if(tripVehicle == null){
            Log.e("CHEAPTRIP","Cannot determine Routes: tripVehicle may not be null");
            return tripRouteList;
        }

        if(startLocation == null){
            Log.e("CHEAPTRIP","Cannot determine Routes: StartLocation may not be null");
            return tripRouteList;
        }

        if(startLocation == null){
            Log.e("CHEAPTRIP","Cannot determine Routes: EndLocation may not be null");
            return tripRouteList;
        }

        if(stationList == null){
            Log.e("CHEAPTRIP","Cannot determine Routes: stationList may not be null");
            return tripRouteList;
        }
        if(stationList.size() < 1){
            Log.e("CHEAPTRIP","Cannot determine Routes: stationList has no Elements");
            return tripRouteList;
        }

        Log.d("CHEAPTRIP","NUMBER OF GASSTATIONS: " + stationList.size());

        /*==========================================================================================
         * Generate the Coordinate List for the POST Body of Matrix Request (ORService)
         *========================================================================================*/
        List<TripLocation> matrixCoordinateList = new ArrayList<>();

        matrixCoordinateList.add(startLocation);
        matrixCoordinateList.add(endLocation);

        for(TripGasStation station : stationList) {
            if (station == null) {
                continue;
            }

            List<Double> stationCoordinates = new ArrayList<>();

            double lon = station.getLongitude();
            double lat = station.getLatitdue();

            stationCoordinates.add(lon);
            stationCoordinates.add(lat);

            TripLocation tripLocation = new TripLocation(lat,lon);

            matrixCoordinateList.add(tripLocation);
        }
        /*==========================================================================================
         * Get TripRoutes with Durations and Distances from ORService API
         *========================================================================================*/
        List<Integer> locationPosition = new ArrayList<Integer>();     // Location of start and end
        locationPosition.add(0);
        locationPosition.add(1);

        GeoDirectionMatrixHandler matrixHandler = new GeoDirectionMatrixHandler(matrixCoordinateList, locationPosition, null,false);
        List<TripRoute> tripRouteList = matrixHandler.makeSyncRequest();


        for(int i = 0 ;i < 10; i++){
            matrixHandler = new GeoDirectionMatrixHandler(matrixCoordinateList, locationPosition, null,false);
            tripRouteList = matrixHandler.makeSyncRequest();
        }
        /*==========================================================================================
         * Initialize the TripRoute ( Set Stops and Costs)
         *========================================================================================*/
        List<TripRoute> resultList = new ArrayList<>();         // list with all routes that hold costs

        Iterator tripListIterator = tripRouteList.iterator();
        /*==========================================================================================
         * Iterate over the stations and calculate the costs
         *========================================================================================*/
        for(TripGasStation tripGasStation : stationList){

            if(!tripGasStation.isOpen()){
                continue;
            }

            TripRoute tripRoute = (TripRoute) tripListIterator.next();

            if(tripRoute == null){
                Log.w("CHEAPTRIP", "RouteService->determineTripRoutes: tripRoute is null. " +
                        "This should not happen (stationList size and tripRoute List size shout be the same");
                continue;
            }
            /*=========================================================
             * Determine Consumption
             *=========================================================*/
            double distance = tripRoute.getDistance();
            double duration = tripRoute.getDuration();

            tripRoute.addTripLocation(startLocation,tripGasStation,endLocation);

            double avgSpeed = distance/(duration/3600);     // in KMH

            double cityMPG = tripVehicle.getFuelConsumptionCity();
            double highwayMPG = tripVehicle.getFuelConsumptionHighway();

            double avgConsumption = interpolateConsumption(avgSpeed,cityMPG,highwayMPG);

            GasStationClient.FuelType fuelType = tripVehicle.getFueltype();
            /*=========================================================
             * Get GasStation Price
             *=========================================================*/
            Double pricePerLiter = null;

            if(fuelType == GasStationClient.FuelType.E5) {
                pricePerLiter = tripGasStation.getPriceE5();
            }

            if(fuelType == GasStationClient.FuelType.E10) {
                pricePerLiter = tripGasStation.getPriceE10();
            }

            if(fuelType == GasStationClient.FuelType.DIESEL) {
                pricePerLiter = tripGasStation.getPriceDiesel();
            }

            if(pricePerLiter == null){
                continue;
            }
            /*=========================================================
             * Determine Costs
             *=========================================================*/
            double costs = calculateCostsForRoute(distance,pricePerLiter,avgConsumption);

            if(costs == 0){
                continue;
            }

            tripRoute.setCosts(costs);
            resultList.add(tripRoute);
        }
        return resultList;
    }

    /**
     * TODO: Document
     * @param calcLocation
     * @return
     */
    public List<TripGasStation> getStationsInRange(TripLocation calcLocation){
        double lat = calcLocation.getLatitdue();
        double lon = calcLocation.getLongitude();

        GasStationForRadiusHandler gasStationHandler = new GasStationForRadiusHandler(context, lat,lon, GAS_STATION_SEARCH_RADIUS,tripVehicle.getFueltype());

        List<TripGasStation> tripGasStations = gasStationHandler.makeSyncRequest();

        return tripGasStations;
    }

    /**
     * Determines the Average consumption by interpolating
     * the values
     *      *
     * @param avgSpeed
     * @param cityMPG
     * @param highwayMPG
     * @return
     */
    public static Double interpolateConsumption(double avgSpeed, double cityMPG, double highwayMPG){
        double citySpeed = 50.0;
        double highwaySpeed = 100.0;

        double avgConsumtion = cityMPG + ( avgSpeed - citySpeed) * (highwayMPG - cityMPG)/ (highwaySpeed -citySpeed);

        return avgConsumtion;
    }

    /**
     * Gets a List of the N Cheapest Routes in the List provided by Argument
     *
     * @param tripRouteList     List of TripRoute to get a List of the Cheapest N Routes for
     * @param topN              Number of TripRoutes to be in the list (The N Cheapest)
     *
     * @return
     */
    public static List<TripRoute> getCheapestRoutes(List<TripRoute> tripRouteList, int topN){
        Comparator<TripRoute> comparator = new Comparator<TripRoute>() {
            @Override
            public int compare(TripRoute tripRoute_1, TripRoute tripRoute_2) {
                if(tripRoute_2 == null || tripRoute_2.getCosts() <= 0){
                    return 1;
                }

                if(tripRoute_1 == null || tripRoute_1.getCosts() <= 0){
                    return -1;
                }

                return Double.compare(tripRoute_1.getCosts() , tripRoute_2.getCosts());
            }
        };

        List<TripRoute> resultTripRoute = new ArrayList<>(tripRouteList);
        Collections.sort(resultTripRoute,comparator);

        return resultTripRoute;
    }

    public static double calculateCostsForRoute( double distance , double pricePerLiter, double  avgConsumption){
        return distance * pricePerLiter * avgConsumption/100;
    }

    public static double determineMaxRange(TripVehicle tripVehicle, TripRoute tripRoute){
        double distance = tripRoute.getDistance();
        double duration = tripRoute.getDuration();

        double hours = duration /3600;
        double avgSpeed = distance/hours;

        double percentRest = tripVehicle.getRemainFuelPercent();
        double cityMPG = tripVehicle.getFuelConsumptionCity();
        double highwayMPG = tripVehicle.getFuelConsumptionCity();

        double avgConsumption = interpolateConsumption(avgSpeed,cityMPG,highwayMPG);
        //double avgConsumption =  interpolateConsumption();

        double maxRange = percentRest * tripVehicle.getTANK_CAPACITY() * avgConsumption;

        return maxRange;
    }

    public static TripLocation findPointfromDistance(double radius, List<TripLocation> tripLocations, double offset){
        int indexLowerBound = 0;
        int indexUpperBound = tripLocations.size()-1;
        int indexMiddle;

        TripLocation startLocation = tripLocations.get(indexLowerBound);
        TripLocation middleLocation = tripLocations.get(indexUpperBound);

        TripLocation lowerLocation = startLocation;
        double distance = calculateDistance(startLocation, middleLocation);

        double areaToFind =  radius - offset;


        double ratio = Math.abs(distance - areaToFind)/areaToFind;

        // > 1 because of rounding
        while( Math.abs(indexLowerBound - indexUpperBound) > 1){
            indexMiddle =(indexUpperBound +indexLowerBound )/2;

            //lowerLocation = tripLocations.get(indexLowerBound);
            middleLocation = tripLocations.get(indexMiddle);

            distance = calculateDistance(startLocation, middleLocation);

            if(areaToFind < distance){
                indexUpperBound = indexMiddle;
            }else{
                indexLowerBound = indexMiddle;
            }

        }

        return  middleLocation;
    }


    public static double calculateDistance(TripLocation location1, TripLocation location2){
        double latitude1 = location1.getLatitdue();
        double longitude1 = location1.getLongitude();

        double latitude2 = location2.getLatitdue();
        double longitude2 = location2.getLongitude();

        double theta = longitude1 - longitude2;
        double dist = Math.sin(deg2rad(latitude1)) * Math.sin(deg2rad(latitude2)) + Math.cos(deg2rad(latitude1)) * Math.cos(deg2rad(latitude2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;

        return (dist);
    }

    public static List<Station> loadHistoricPrices(int year, int month, int day){
        GasStationHistoryPriceHandler gasStationHistoryPriceHandler = new GasStationHistoryPriceHandler(year,month,day);
        List<Station> priceList = gasStationHistoryPriceHandler.getHistory(year, month, day);

        return priceList;
    }

    public static List<Station> loadHistoricStationProperties(int year, int month, int day){
        GasStationHistoryHandler gasStationHistoryHandler = new GasStationHistoryHandler(2019,12, 10);
        List<Station> stationList = gasStationHistoryHandler.getHistory();

        return stationList;
    }
    private double convertBarrelsToLitre(double barrels){
        return barrels/0.0062898;
    }

    private double convertMilesToKM(double miles){
        return 1.60934 * miles;
    }

    private double convertMPGto100KML(double mpg){
        return 235.215/mpg;
    }



    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if(context != null && context instanceof CalculationActivity)
            calculationListener.onCalculationDone(tripRouteList);
    }
}
