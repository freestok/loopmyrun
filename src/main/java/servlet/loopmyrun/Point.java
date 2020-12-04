package servlet.loopmyrun;

public class Point {
    private double x;
    private double y;
    public Point (
            double x,
            double y
    ) {
        this.x = x;
        this.y = y;
    }

    public double x() { return this.x; }
    public double y() { return this.y; }

    public String coords() {
        String c = this.x + "," + this.y;
        return c;
    }
}


