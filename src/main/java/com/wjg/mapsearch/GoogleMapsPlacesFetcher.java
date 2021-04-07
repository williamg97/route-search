package com.wjg.mapsearch;

import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.TextSearchRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import io.jenetics.jpx.WayPoint;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class GoogleMapsPlacesFetcher {

    private static final Logger logger = LoggerFactory.getLogger(GoogleMapsPlacesFetcher.class);
    private GeoApiContext context = new GeoApiContext.Builder().apiKey("AIzaSyDFTxyOhsF2cTFSSsGXy5zngdAmI10jTAQ").build();

    // 5000 calls are free a month
    // $40 per 1000

    public List<PlacesSearchResult> getPlacesNearbyFromWaypoints(List<WayPoint> waypoints, int radiusMetres, PlaceType type) throws InterruptedException, ApiException, IOException {
        logger.info("Google Maps API: Getting places from waypoints for type: " + type.name() + "...");
        List<LatLng> convertedWaypoints = waypointsToLatLng(waypoints);
        List<PlacesSearchResult> results = new ArrayList<>();
        AtomicInteger requestCount = new AtomicInteger(0);
        for(LatLng latLng : convertedWaypoints) {
            logger.info("Google Maps API: Getting places from waypoint: " + latLng + " for type: " + type + "; Request Count is: " + requestCount.intValue());
            results.addAll(getResultsForLatLng(latLng, radiusMetres, type, requestCount));
        }
        deduplicateResults(results);
        return results;
    }

    public List<PlacesSearchResult> getTextSearchPlaces(String searchText, List<WayPoint> waypoints, int radiusMetres) throws InterruptedException, ApiException, IOException {
        logger.info("Google Maps API: Getting places from waypoints for type...");
        List<LatLng> convertedWaypoints = waypointsToLatLng(waypoints);
        List<PlacesSearchResult> results = new ArrayList<>();
        for(LatLng latLng : convertedWaypoints) {
            logger.info("Google Maps API: Getting places from waypoint: " + latLng + " for: " + searchText);
            results.addAll(getStringSearch(searchText, latLng, radiusMetres));
        }
        deduplicateResults(results);
        return results;
    }

    public void closeContext() {
        context.shutdown();
    }

    private List<PlacesSearchResult> getResultsForLatLng(LatLng latLng, int radiusMetres, PlaceType type, AtomicInteger requestCount) throws InterruptedException, ApiException, IOException {
        PlacesSearchResponse placesResponse = PlacesApi.nearbySearchQuery(context, latLng).radius(radiusMetres).type(type).await();
        requestCount.set(requestCount.intValue()+1);
        List<PlacesSearchResult> foundPlaces = new ArrayList<>();
        do {
            if (placesResponse.results.length > 0) {
                foundPlaces.addAll(Arrays.asList(placesResponse.results));
            }
            if(placesResponse.nextPageToken != null) {
                logger.info("Multiple pages; requesting again for type: " + type.name());
                Thread.sleep(5000);
                placesResponse = PlacesApi.nearbySearchQuery(context, latLng).pageToken(placesResponse.nextPageToken).await(); // type(placeType)
                requestCount.set(requestCount.intValue()+1);
            }
        } while (placesResponse.nextPageToken != null);
        return foundPlaces;
    }

    private List<PlacesSearchResult> getStringSearch(String searchText, LatLng latLng, int radiusMetres) throws InterruptedException, ApiException, IOException {
        PlacesSearchResponse placesResponse = PlacesApi.textSearchQuery(context, searchText, latLng).radius(radiusMetres).await();
        List<PlacesSearchResult> foundPlaces = new ArrayList<>();
        do {
            if (placesResponse.results.length > 0) {
                foundPlaces.addAll(Arrays.asList(placesResponse.results));
            }
            if(placesResponse.nextPageToken != null) {
                logger.info("Multiple pages; requesting again for string: " + searchText);
                Thread.sleep(5000);
                placesResponse = PlacesApi.nearbySearchQuery(context, latLng).pageToken(placesResponse.nextPageToken).await(); // type(placeType)
            }
        } while (placesResponse.nextPageToken != null);
        return foundPlaces;
    }

    private void deduplicateResults(List<PlacesSearchResult> searchResults) {
        HashSet<Object> seen=new HashSet<>();
        searchResults.removeIf(e->!seen.add(e.placeId) || e.permanentlyClosed);
    }

    private List<LatLng> waypointsToLatLng(List<WayPoint> wayPoints) {
        return wayPoints.stream().map(wayPoint -> new LatLng(wayPoint.getLatitude().doubleValue(), wayPoint.getLongitude().doubleValue())).collect(Collectors.toList());
    }
}
