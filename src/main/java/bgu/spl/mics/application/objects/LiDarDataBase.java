package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
        //Fileds:
        private static LiDarDataBase instance; // Singleton instance
        private List<StampedCloudPoints> cloudPoints; // List of all cloud points  
        private AtomicInteger counter;                  

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        if (instance == null) {
            synchronized (LiDarDataBase.class) {
                if (instance == null) {
                    instance = new LiDarDataBase(filePath);
                }
            }
        }
        return instance;
    }

    /********************************************* Constrector ***************************************************/
    private LiDarDataBase(String filePath) {
        cloudPoints = new ArrayList<>();
        counter = new AtomicInteger(0);
        loadCloudPointsFromFile(filePath);
    }

    /********************************************* Methods ***************************************************/
    private void loadCloudPointsFromFile(String filePath) {
    Gson gson = new Gson();
    try (FileReader reader = new FileReader(filePath)) {
        // Deserialize JSON into List of StampedCloudPoints
        JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
        for (JsonElement element : jsonArray) {
            JsonObject obj = element.getAsJsonObject();
            String id = obj.get("id").getAsString();
            int time = obj.get("time").getAsInt();
            JsonArray pointsArray = obj.getAsJsonArray("cloudPoints");

            // Convert JSON array of points into List<CloudPoint>
            List<CloudPoint> cloudPointsList = new ArrayList<>();
            for (JsonElement pointElement : pointsArray) {
                JsonArray pointCoords = pointElement.getAsJsonArray();
                double x = pointCoords.get(0).getAsDouble();
                double y = pointCoords.get(1).getAsDouble();
                CloudPoint cloudPoint = new CloudPoint(x, y);
                cloudPointsList.add(cloudPoint);
            }

            // Create a StampedCloudPoints object and add it to the list
            StampedCloudPoints stampedCloudPoints = new StampedCloudPoints(id, time, cloudPointsList);
            cloudPoints.add(stampedCloudPoints);
        }
    } catch (IOException e) {
        System.err.println("Error reading LiDAR data JSON file: " + e.getMessage());
    }
}
    /**
     * Retrieves the {@link StampedCloudPoints} object corresponding to the specified {@code time}.
     *
     * @param time the timestamp for which the {@link StampedCloudPoints} is requested.
     * @param id the id for which the {@link StampedCloudPoints} is requested.
     * @return the {@link StampedCloudPoints} whose {@code time} matches the specified {@code time},
     * and {@code id} matches the specified {@code id}
     *  or {@code null} if no matching {@link StampedCloudPoints} is found.
     */
    public StampedCloudPoints getStampedCloudPoints(int time, String id) {
        for (StampedCloudPoints point : cloudPoints) {
            if (point.getTime() == time && point.getId().equals(id)) {
                // Incremante the counter by 1 (using cas to sync):
                counter.incrementAndGet();
                return point;
            }
        }
        return null; // No matching point found witch mean there is error
    }
    public int getLastTime(){ //need to fix
        return cloudPoints.get(cloudPoints.size()-1).getTime();
    }

    public boolean isFinished(){
        return counter.get() > getLastTime();
    }

    public static void resetInstance() {
        instance = null;
    }
    
}
