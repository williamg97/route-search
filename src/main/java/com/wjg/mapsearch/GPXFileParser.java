package com.wjg.mapsearch;

import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResult;
import io.jenetics.jpx.*;
import io.jenetics.jpx.geom.Geoid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GPXFileParser {

    private static final Logger logger = LoggerFactory.getLogger(GPXFileParser.class);

    public List<WayPoint> getGpxWaypoints() throws IOException {
        logger.info("Getting GPX waypoints...");
        try(InputStream is = getClass().getClassLoader().getResourceAsStream("route.gpx")) {
            ArrayList<WayPoint> latLong = new ArrayList<>();
            GPX.read(is).tracks().flatMap(Track::segments).flatMap(TrackSegment::points).forEach(latLong::add);
            return latLong;
        }
    }

    public Length getGpxLength() throws IOException {
        logger.info("Getting GPX length...");
        try(InputStream is = getClass().getClassLoader().getResourceAsStream("route.gpx")) {
            return GPX.read(is).tracks().flatMap(Track::segments).findFirst().map(TrackSegment::points).orElse(Stream.empty()).collect(Geoid.WGS84.toPathLength());
        }
    }

    public void writePointsFromGooglePlaceResult(List<PlacesSearchResult> places, String type) throws IOException {
        logger.info("Writing GPX waypoints for " + type + "...");
        GPX.Builder gpx = GPX.builder();
        for(PlacesSearchResult place : places) {
            if(place != null && place.geometry != null) {
                WayPoint wayPoint = WayPoint.builder().name(place.name).desc(getDescription(place)).build(place.geometry.location.lat, place.geometry.location.lng);
                gpx.addWayPoint(wayPoint);
            }
        }
        GPX.write(gpx.build(), Path.of("output_" + type + ".gpx"));
    }

    private String getDescription(PlacesSearchResult result) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Status: " + (result.businessStatus != null ? result.businessStatus : "Unknown") + "; ");
        stringBuilder.append("Rating: " + result.rating + "; ");
        //stringBuilder.append("Opening Hours: " + (result.openingHours != null ? result.openingHours.toString() : "Unknown"));
        return stringBuilder.toString();
    }

    public void writeSamplingPoints(List<WayPoint> places) throws IOException {
        logger.info("Writing GPX sampling waypoints...");
        GPX.Builder gpx = GPX.builder();
        for(WayPoint place : places) {
            gpx.addWayPoint(place);
        }
        GPX.write(gpx.build(), Path.of("output-waypoints.gpx"));
    }


}
