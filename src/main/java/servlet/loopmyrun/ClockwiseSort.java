package servlet.loopmyrun;

import java.util.Comparator;

public class ClockwiseSort implements Comparator<LatLng> {
    private LatLng origin;

    public ClockwiseSort(LatLng origin) {
        this.origin = origin;
    }
    @Override
    public int compare(LatLng p1, LatLng p2) {
        double angle1 = clockwise(p1,this.origin);
        double angle2 = clockwise(p2,this.origin);
        if (angle1 == angle2) {
            return 0;
        } else if (angle1 > angle2) {
            return 1;
        } else {
            return -1;
        }
    }

    private double clockwise(LatLng p, LatLng o) {
        // credit to https://stackoverflow.com/questions/41855695/sorting-list-of-two-dimensional-coordinates-by-clockwise-angle-using-python
        int[] refVec = {0, 1};
        double[] vector = {p.y() - o.y(), p.x() - o.x()};
        double lenVector = Math.hypot(vector[0], vector[1]);

        if (lenVector == 0) { return Math.PI * -1; }

        double[] normalized = {vector[0]/lenVector, vector[1]/lenVector};
        double dotProd = normalized[0] * refVec[0] + normalized[1] * refVec[1];
        double diffProd = refVec[1] * normalized[0] - refVec[0] * normalized[1];
        double angle = Math.atan2(diffProd, dotProd);

        if (angle < 0) { return 2 * Math.PI + angle; }

        return angle;
    }
}
