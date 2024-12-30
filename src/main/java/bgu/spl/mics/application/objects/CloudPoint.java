package bgu.spl.mics.application.objects;

/**
 * CloudPoint represents a specific point in a 3D space as detected by the LiDAR.
 * These points are used to generate a point cloud representing objects in the environment.
 */
public class CloudPoint {
    //Fileds:
    double x;
    double y;

    /********************************************* Constrector ***************************************************/
    public CloudPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /********************************************* Methods ***************************************************/
    // Getters
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // Setters
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
