package bgu.spl.mics;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	//Singleton fileds:
	private static MessageBusImpl instance = null;
	private static final Object lock = new Object();

	//Class Dete Strecture:
	private final ConcurrentHashMap<MicroService, LinkedList<Message>> messageQueue;
	//Brodcast:
	private final ConcurrentHashMap<Class <? extends Broadcast>, LinkedList<MicroService>> brodSub;
	private final ConcurrentHashMap<MicroService, LinkedList<Class <? extends Broadcast>>> microToBrod;
	//Event
	private final ConcurrentHashMap<Class <? extends Event<?>>, BlockingQueue<MicroService>> eventSub;
	private final ConcurrentHashMap<MicroService, LinkedList<Class <? extends Event<?>>>> microToEvent;
	private final ConcurrentHashMap<Event<?>, Future<?>> eventToFuture;

	/********************************************* Constrector ***************************************************/
	private MessageBusImpl() { //Using private constractor to make sure there is single instance from this class
		messageQueue = new ConcurrentHashMap<>();
		brodSub = new ConcurrentHashMap<>();
		eventSub = new ConcurrentHashMap<>();
		eventToFuture= new ConcurrentHashMap<>();
		microToBrod = new ConcurrentHashMap<>();
		microToEvent = new ConcurrentHashMap<>();
	}

	//Singleton
	public static MessageBusImpl getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new MessageBusImpl();
                }
            }
        }
        return instance;
    }

	/********************************************* Methods ***************************************************/
	@Override
	public synchronized <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized(m) {
			eventSub.putIfAbsent(type, new LinkedBlockingQueue<MicroService>());
			microToEvent.putIfAbsent(m, new LinkedList<Class <? extends Event<?>>>());
			synchronized(eventSub.get(type)){
				eventSub.get(type).add(m);
				microToEvent.get(m).add(type);
			}
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		brodSub.putIfAbsent(type, new LinkedList<MicroService>());
		microToBrod.putIfAbsent(m, new LinkedList<Class <? extends Broadcast>>());
		synchronized(brodSub.get(type)){
			synchronized(m) {
				brodSub.get(type).add(m);
				microToBrod.get(m).add(type);
			}
		}
	}
	

	@Override
	public <T> void complete(Event<T> e, T result) {
		//updating the result of the event in the coresponded future
		Future<T> future = (Future<T>) eventToFuture.get(e);
		future.resolve(result);
		eventToFuture.remove(e);
	}

	@Override
public void sendBroadcast(Broadcast b) {
    LinkedList<MicroService> list = brodSub.get(b.getClass());
    synchronized (list) {
		if (list != null  && !list.isEmpty()) {
			for (MicroService m : list) {
				synchronized (m) {
					LinkedList<Message> queue = messageQueue.get(m);
					if (queue != null) {
						queue.add(b); // Add broadcast to each subscriber's queue
						m.notifyAll(); // Notify subscribers waiting for messages
					}
				}
			}
		}  
    }
}


	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		BlockingQueue<MicroService> q = eventSub.get(e.getClass()); //Getting the all the micro servers that subscribed to this type of event
		if (q == null || q.isEmpty()) 
			return null;
		MicroService m = null;
		try{
			m = q.take(); //chossing the first micro server
			q.put(m);//put it in the end to maintain round robbin 
		}
		catch (InterruptedException ex){}
		
		Future<T> future = new Future<>(); 
		synchronized(m){ 
			eventToFuture.putIfAbsent(e, future); // linking the futer and the event 
			messageQueue.get(m).add(e); // add the event to the meessege queue of micro servers
			m.notifyAll(); //notify for the awaitMessage method
		}
		return future;
	
}

	@Override
	public void register(MicroService m) {
		synchronized(m){
			messageQueue.putIfAbsent(m, new LinkedList<Message>());
		}

	}

	@Override
public void unregister(MicroService m) {
    synchronized (m) {
        // Remove the microservice from the message queue
        messageQueue.remove(m);

        // Remove the microservice from all broadcast subscriptions
        LinkedList<Class<? extends Broadcast>> broadcastList = microToBrod.get(m);
        if (broadcastList != null) {
			for (Class<? extends Broadcast> broadcastType : broadcastList) {
				LinkedList<MicroService> subscribers = brodSub.get(broadcastType);                  
				synchronized (subscribers) {
					subscribers.remove(m);
				}                    
			}
            
            // Remove the microservice entry from microToBrod
                microToBrod.remove(m);
        }

        // Remove the microservice from all event subscriptions
        LinkedList<Class<? extends Event<?>>> eventList = microToEvent.get(m);
        if (eventList != null) {
			for (Class<? extends Event<?>> eventType : eventList) {
				BlockingQueue<MicroService> subscribers = eventSub.get(eventType);
					synchronized (subscribers) {
						subscribers.remove(m);
					}				
			}
            // Remove the microservice entry from microToEvent
            microToEvent.remove(m);
        }
    }
}


	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		synchronized(m){
			//throw the {@link IllegalStateException} in the case where m was never registered.
			LinkedList<Message> queue = messageQueue.get(m);
			if (queue == null) {
				throw new IllegalStateException("the microService:" + m.getName() +" is unregister.");
			}
			//waiting for new message to be send
			while (queue.isEmpty()) { 
				try{
					m.wait(); // if there is no new message, wait for it
				}
				catch (InterruptedException e){}
			
			}
			return queue.poll();
		}
	}
	// getters for testing 
	public LinkedList<Message> getMessageQueue(MicroService m){
		return messageQueue.get(m);
	}

	public LinkedList<MicroService> getBrodSub(Class <? extends Broadcast> type){
		return brodSub.get(type);
	}

	public BlockingQueue<MicroService> getEventSub(Class <? extends Event<?>> type){
		return eventSub.get(type);
	}

	public Future<?> getFuture(Event<?> event){
		return eventToFuture.get(event);
	}

	public LinkedList<Class <? extends Broadcast>> getMicroToBrod(MicroService m){
		return microToBrod.get(m);
	}

	public LinkedList<Class <? extends Event<?>>> getMicroToEvent(MicroService m){
		return microToEvent.get(m);
	}



}

