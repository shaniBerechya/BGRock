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
	private static MessageBusImpl instance = null;
	private static final Object lock = new Object();
	private final ConcurrentHashMap<MicroService, BlockingQueue<Broadcast>> brodQ;
	private final ConcurrentHashMap<Class <? extends Broadcast>, LinkedList<MicroService>> brodSub;
	private final ConcurrentHashMap<MicroService, BlockingQueue<Event<?>>> eventQ;
	private final ConcurrentHashMap<Class <? extends Event<?>>, BlockingQueue<MicroService>> eventSub;
	private final ConcurrentHashMap<Event<?>, Future<?>> eventToFuture;

	private MessageBusImpl() { //Using private constractor to make sure there is single instance from this class
		brodQ = new ConcurrentHashMap<>();
		brodSub = new ConcurrentHashMap<>();
		eventQ = new ConcurrentHashMap<>();
		eventSub = new ConcurrentHashMap<>();
		eventToFuture= new ConcurrentHashMap<>();
	}
	
	@Override
	public synchronized <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized(m) {
			eventSub.putIfAbsent(type, new LinkedBlockingQueue<MicroService>());
			eventSub.get(type).add(m);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		brodSub.putIfAbsent(type, new LinkedList<MicroService>());
		synchronized(brodSub.get(type)){
			synchronized(m) {
				brodSub.get(type).add(m);
			}
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		//updating the result of the event in the coresponded future
		Future future = eventToFuture.get(e);
		future.resolve(result);
		eventToFuture.remove(e);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		LinkedList<MicroService> list = brodSub.get(b.getClass()); //getting the list of the micro servers that subscribe to this brodcust
		synchronized(list){ //list is not thread safe
			for(MicroService m: list){
				synchronized(m){ 
					brodQ.get(m).add(b); // adding this brodcust to each micro that suscribe to it
					m.notifyAll(); //notify for the awaitMessage method
				}
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		BlockingQueue<MicroService> q = eventSub.get(e.getClass()); //Getting the all the micro servers that subscribed to this type of event
		MicroService m = null;
		try{
			m = q.take(); //chossing the first micro server
		}
		catch (InterruptedException ex){}
		
		Future<T> future = new Future<>(); 
		synchronized(m){ 
			eventToFuture.putIfAbsent(e, future); // linking the futer and the event 
			eventQ.get(m).add(e); // add the event to the meessege queue of micro servers
			try{
				q.put(m); //adding it to the end of the queue to keep round robbin method
			}
			catch (InterruptedException ex){}
			m.notifyAll(); //notify for the awaitMessage method
		}
		return future;
	}

	@Override
	public void register(MicroService m) {
		synchronized(m){
			eventQ.putIfAbsent(m, new LinkedBlockingQueue<Event<?>>());
			brodQ.putIfAbsent(m, new LinkedBlockingQueue<Broadcast>());
		}

	}

	@Override
	public void unregister(MicroService m) {
		//Remove the micro server m from their quque
		synchronized(m){
			eventQ.remove(m);
			brodQ.remove(m);
		}

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		synchronized(m){
			//throw the {@link IllegalStateException} in the case where m was never registered.
			if (eventQ.get(m) == null && brodQ.get(m) == null){
				throw new IllegalStateException("the microService:" + m.getName() +" is unregister.");
			}
			//finding the message queue of the micro server m
			BlockingQueue<Event<?>> eventQueue = eventQ.get(m);
			BlockingQueue<Broadcast> brodQueue = brodQ.get(m);
			
			//waiting for new message to be send
			while (eventQueue.isEmpty() || brodQueue.isEmpty()) { 
				try{
					m.wait(); // if there is no new message, wait for it
				}
				catch (InterruptedException e){}
			}
			//Checking where is the new message
			if (!brodQueue.isEmpty()){
				return brodQueue.take();
			}
			else{
				return eventQueue.take();
			}
		}

	}
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

	

}
