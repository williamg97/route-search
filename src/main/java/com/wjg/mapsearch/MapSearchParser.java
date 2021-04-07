package com.wjg.mapsearch;

import com.google.maps.PlaceDetailsRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResult;
import io.jenetics.jpx.Length;
import io.jenetics.jpx.WayPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class MapSearchParser {

    private static final Logger logger = LoggerFactory.getLogger(MapSearchParser.class);

    private static final int RADIUS = 12000; // In metres. For nearby search API.
    private static final int RADIUS_TEXT = 6000; // In metres. Radius to search in text bases searchs

    private static final PlaceType[] TYPES_TO_SEARCH = new PlaceType[] {};

    // Enter places to search
    //private static final PlaceType[] TYPES_TO_SEARCH = new PlaceType[] { PlaceType.BICYCLE_STORE, PlaceType.BAKERY, PlaceType.BAR, PlaceType.TOURIST_ATTRACTION, PlaceType.CAFE, PlaceType.CONVENIENCE_STORE, PlaceType.RESTAURANT, PlaceType.GROCERY_OR_SUPERMARKET, PlaceType.LIQUOR_STORE, PlaceType.GAS_STATION };
    //private static final PlaceType[] TYPES_TO_SEARCH = new PlaceType[] { PlaceType.BAR };
    //    private static final PlaceType[] TYPES_TO_SEARCH = new PlaceType[] {
    //            PlaceType.MEAL_TAKEAWAY,
    //            PlaceType.MEAL_DELIVERY,
    //            PlaceType.NIGHT_CLUB, PlaceType.SPA };

    private GPXFileParser gpxFileParser;
    private GoogleMapsPlacesFetcher googleMapsPlacesFetcher;

    @Autowired
    public MapSearchParser(GPXFileParser gpxFileParser, GoogleMapsPlacesFetcher googleMapsPlacesFetcher) {
        this.gpxFileParser = gpxFileParser;
        this.googleMapsPlacesFetcher = googleMapsPlacesFetcher;
    }

    private void run() {
        try {
            List<WayPoint> waypoints = gpxFileParser.getGpxWaypoints();
            Length length = gpxFileParser.getGpxLength();
            int calculateSampleRate = getSampleRateFromRadiusAndLength(length.doubleValue(), RADIUS);
            List<WayPoint> sampledWaypoints = sampleWaypoints(waypoints, calculateSampleRate);
            gpxFileParser.writeSamplingPoints(sampledWaypoints);

            logger.info("Number of requests will be at least: " + (sampledWaypoints.size() * TYPES_TO_SEARCH.length));

            // If you want to do text searchs uses this
            //fetchAndSaveTextSearch("distillery", sampledWaypoints);
            //fetchAndSaveTextSearch("toilets", sampledWaypoints);

            for(PlaceType type : TYPES_TO_SEARCH) {
                try {
                    List<PlacesSearchResult> searchResults = googleMapsPlacesFetcher.getPlacesNearbyFromWaypoints(sampledWaypoints, RADIUS, type);
                    gpxFileParser.writePointsFromGooglePlaceResult(searchResults, type.name());
                } catch (Exception e) {
                    logger.error("Error getting results for type: " + type.name(), e);
                }
            }
            googleMapsPlacesFetcher.closeContext();
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }

    private void fetchAndSaveTextSearch(String searchText, List<WayPoint> wayPoints) throws IOException, ApiException, InterruptedException {
        List<PlacesSearchResult> result = googleMapsPlacesFetcher.getTextSearchPlaces(searchText, wayPoints, RADIUS_TEXT);
        gpxFileParser.writePointsFromGooglePlaceResult(result, searchText);
    }

    private int getSampleRateFromRadiusAndLength(double length, int radius) {
        return (int) Math.floor(length / radius);
    }

    private List<WayPoint> sampleWaypoints(List<WayPoint> fullWaypointList, int sampleRate) {
        ArrayList<WayPoint> smallerWaypointList = new ArrayList<>();
        for(int i=0; i<fullWaypointList.size(); i=i+sampleRate) {
            smallerWaypointList.add(fullWaypointList.get(i));
        }
        return smallerWaypointList;
    }
}
