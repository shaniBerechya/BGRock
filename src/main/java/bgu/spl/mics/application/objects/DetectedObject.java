package bgu.spl.mics.application.objects;

/**
 * DetectedObject represents an object detected by the camera.
 * It contains information such as the object's ID and description.
 */
public class DetectedObject {
    //Fileds:
    private String id;
    private String description;

    /********************************************* Constrector ***************************************************/
    public DetectedObject(String id, String description ){
        this.id = id;
        this.description = description;
    }

    /********************************************* Methods ***************************************************/
    public String getId(){
        return id;
    }
    
    public String getDescription(){
        return description;
    }
}
