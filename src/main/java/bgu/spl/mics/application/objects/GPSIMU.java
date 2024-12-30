package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    //Fileds:
    int currentTick;
    String poseDatasPath;

    enum status {
        Up, Down, Eror
    }
    List<Pose> PoseList;

    /********************************************* Constrector ***************************************************/
    /**
     * Constructs a Camera object with the given parameters.
     *
     * @param id              The unique identifier of the camera.
     * @param frequency       The frequency at which the camera operates.
     * @param poseDatasPath The file path where the GPSIMU's data is stored.
     */
    public GPSIMU(int id,int frequency,String poseDatasPath){
        //TO DO
    }

    /********************************************* Methods ***************************************************/

    /**
     * Prepares the list of pose for a given time by reading the data from the GPSIMU's data path.
     * 
     * @param time The time at which to search for the detected objects.
     * @post The last pose object in {@code PoseList} should have a timestamp equal to {@code time}.
     */
    public void preparesDate(int time){
        //TO DO
    }

    /**
    * Retrieves the pose from the {@code PoseList} that matches the {@code time}.
    *
    * @param time current time.
    * @return the {@code Pose} in {@code PoseList} corresponding to the given {@code time}.
    */
    public Pose getPose(int time){
        //TO DO
        return null;
    }
}
