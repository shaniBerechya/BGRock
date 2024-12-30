package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
        //Fileds:
        private List<StampedCloudPoints>  cloudPoints;
        private int index;

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        // TODO: Implement this
        return null;
    }


    /********************************************* Constrector ***************************************************/
    private LiDarDataBase(String filePath){
        //TO DO
        index = 0;
    }

    /********************************************* Methods ***************************************************/
    /**
     * Retrieves the {@link StampedCloudPoints} object corresponding to the specified {@code time}.
     *
     * @param time the timestamp for which the {@link StampedCloudPoints} is requested.
     * @param id the id for which the {@link StampedCloudPoints} is requested.
     * @return the {@link StampedCloudPoints} whose {@code time} matches the specified {@code time},
     * and {@code id} matches the specified {@code id}
     *  or {@code null} if no matching {@link StampedCloudPoints} is found.
     */
    public StampedCloudPoints getStampedCloudPoints(int time,  String id) {
        // TO DO
        return null;
    }
}
