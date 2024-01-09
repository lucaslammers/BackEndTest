package Ontdekstation013.ClimateChecker.utility;

import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodCoords;

import java.util.List;

public class GpsTriangulation {
    public static boolean pointInPolygon(List<NeighbourhoodCoords> coords, float[] point) {
        float[][] polygon = new float[coords.size()][];

        for (int i = 0; i < coords.size(); i++) {
            NeighbourhoodCoords coord = coords.get(i);
            polygon[i][0] = coord.getLatitude();
            polygon[i][1] = coord.getLongitude();
        }

        return pointInPolygon(polygon, point);
    }

    // Algorithm for finding a gps point inside an area
    public static boolean pointInPolygon(float[][] polygon, float[] point) {
        boolean odd = false;
        for (int i = 0, j = polygon.length - 1; i < polygon.length; i++) {
            if (((polygon[i][0] > point[0]) != (polygon[j][0] > point[0]))
                    && (point[1] < ((polygon[j][1] - polygon[i][1])
                    * (point[0] - polygon[i][0]) / (polygon[j][0] - polygon[i][0]) + polygon[i][1]))) {
                odd = !odd;
            }
            j = i;
        }
        return odd;
    }
}
