package servlet.loopmyrun;

import java.text.DecimalFormat;

public class LatLng {
    private double x;
    private double y;
    private DecimalFormat df = new DecimalFormat("#.00000");
    public LatLng(
            double x,
            double y
    ) {
        this.x = Double.parseDouble(df.format(x));
        this.y = Double.parseDouble(df.format(y));
    }

    public double x() { return this.x; }
    public double y() { return this.y; }

    public String coords() { return this.x + "," + this.y; }
}


