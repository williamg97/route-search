package com.wjg.mapsearch;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.WayPoint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GPXFileMerger {

    public void mergeGpxFiles(String file1, String file2, String outputFileName) throws IOException {
        try(InputStream file1Is = getClass().getClassLoader().getResourceAsStream(file1)) {
            try(InputStream file2Is = getClass().getClassLoader().getResourceAsStream(file2)) {
                ArrayList<WayPoint> waypoints = new ArrayList<>();
                GPX.read(file1Is).wayPoints().forEach(waypoints::add);
                GPX.read(file2Is).wayPoints().forEach(waypoints::add);
                GPX.Builder gpx = GPX.builder();
                waypoints.forEach(gpx::addWayPoint);
                deduplicateResults(waypoints);
                GPX.write(gpx.build(), Path.of(outputFileName));
            }
        }
    }

    private void deduplicateResults(List<WayPoint> waypoints) {
        HashSet<Object> seen=new HashSet<>();
        waypoints.removeIf(e->!seen.add(e.getName()));
    }
}
