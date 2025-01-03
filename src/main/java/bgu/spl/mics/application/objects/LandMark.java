package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    //Fileds:
    private String id;
    private String description;
    private List<CloudPoint> coordinates;

    /********************************************* Constrector ***************************************************/
    public LandMark(String id, String description, List<CloudPoint> coordinates){
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    /********************************************* Methods ***************************************************/
    /**
    * Enhances the accuracy of the current {@code coordinates} by averaging them with the provided {@code coordinates}.
    * If a {@code CloudPoint} in the given list does not have a corresponding point in {@code this.coordinates}, 
    * it is added to theend of the current list.
    *
    * @param coordinates the list of {@code CloudPoint} used to improve the accuracy of the current {@code this.coordinates}.
    * @post 
    * - Each {@code CloudPoint} in {@code this.coordinates} is updated to be the average 
    *   of itself and the corresponding {@code CloudPoint} from the provided list.
    * - If a {@code CloudPoint} from the provided list does not have a match in {@code this.coordinates}, 
    *   it is added to {@code this.coordinates}.
    */

    public void improve(List<CloudPoint> newCoordinates) {
        int size = Math.min(coordinates.size(), newCoordinates.size());

        // Average the existing points
        for (int i = 0; i < size; i++) {
            CloudPoint current = coordinates.get(i);
            CloudPoint incoming = newCoordinates.get(i);

            double avgX = (current.getX() + incoming.getX()) / 2;
            double avgY = (current.getY() + incoming.getY()) / 2;

            current.setX(avgX);
            current.setY(avgY);
        }

        // Add any additional points from the incoming list
        for (int i = size; i < newCoordinates.size(); i++) {
            coordinates.add(newCoordinates.get(i));
        }
    }


    //getes:
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public List<CloudPoint> getCoordinates(){
        return coordinates;
    }
}
