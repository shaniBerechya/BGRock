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

}
