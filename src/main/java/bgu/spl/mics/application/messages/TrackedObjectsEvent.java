package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

public class TrackedObjectsEvent implements Event<TrackedObject> {
    private TrackedObject trackedObject;

    public TrackedObjectsEvent(TrackedObject trackedObject){
        this.trackedObject = trackedObject;
    }

    public TrackedObject getEvent(){
        return trackedObject;
    }
 
}
