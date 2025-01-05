package bgu.spl.mics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


//====================================================================================================================

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus
 * interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	//Singleton
	private static class SingletonHolder {
        // The single instance of MessageBusImpl that will be created
        private static final MessageBusImpl INSTANCE = new MessageBusImpl();
	}
	//Class Dete Strecture:
	private final ConcurrentHashMap<MicroService, BlockingQueue<Message>> messageQueue;
	//Brodcast:
	private final ConcurrentHashMap<Class<? extends Broadcast>, ArrayList<MicroService>> brodSub;
	//Event
	private final ConcurrentHashMap<Class<? extends Event<?>>, Queue<MicroService>> eventSub;
	private final ConcurrentHashMap<Event<?>, Future<?>> eventFutureMap;

	
	/********************************************* Constrector ***************************************************/
	private MessageBusImpl() {//Using private constractor to make sure there is single instance from this class
		eventSub = new ConcurrentHashMap<>();
		brodSub = new ConcurrentHashMap<>();
		messageQueue = new ConcurrentHashMap<>();
		eventFutureMap = new ConcurrentHashMap<>();
	}

	// Static method to get the instance of MessageBusImpl
	public static MessageBusImpl getInstance() {
		return SingletonHolder.INSTANCE;
	}
	/********************************************* Methods ***************************************************/

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		eventSub.putIfAbsent(type, new LinkedList<MicroService>());
		Queue<MicroService> queue = eventSub.get(type);
		synchronized (m) {
			synchronized (queue) {
				queue.add(m);
				
			}
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		brodSub.putIfAbsent(type, new ArrayList<MicroService>());
		List<MicroService> list = brodSub.get(type);
		synchronized (m) {
			synchronized (list) {
				list.add(m);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		//updating the result of the event in the coresponded future
		Future<T> future = (Future<T>) eventFutureMap.get(e);
		if (future != null) {
			synchronized (future) {
				future.resolve(result);
			}
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		List<MicroService> brodList = brodSub.get(b.getClass()); 
		if (brodList != null) {
			synchronized (brodList) {
				for (MicroService m : brodList) {
					synchronized (m) {
						try {
							messageQueue.get(m).put(b);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				}
			}
		}
	}


	public <T> Future<T> sendEvent(Event<T> e) {
		Queue<MicroService> queue = eventSub.get(e.getClass()); 
		synchronized (queue) {
			if (queue == null || queue.isEmpty()) {
				return null;
			}
			MicroService m = queue.poll(); 
			synchronized (m) {
				BlockingQueue<Message> microQueue = messageQueue.get(m); 
				try { 
					microQueue.put(e);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
			Future<T> future = new Future<>();
			eventFutureMap.put(e, future);
			queue.add(m);
			return future;
		}
	}

	@Override
	public void register(MicroService m) {
		synchronized (m) {
			messageQueue.putIfAbsent(m, new LinkedBlockingQueue<Message>());
		}
	}

	@Override
	public void unregister(MicroService m) {
		synchronized (m) {
			messageQueue.remove(m);

			for (Queue<MicroService> queue : eventSub.values()) {
				synchronized (queue) {
					queue.remove(m); 
				}
			}
			for (List<MicroService> list : brodSub.values()) {
				synchronized (list) {
					list.remove(m);
				}
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		BlockingQueue<Message> microQueue = messageQueue.get(m);
		Message msg = microQueue.take();
		return msg;
	}

	// getters for testing 
	public BlockingQueue<Message> getMessageQueue(MicroService m) {
		return this.messageQueue.get(m);
	}

	public ArrayList<MicroService> getBrodSub(Class<? extends Broadcast> type) {
		return brodSub.get(type);
	}

	public Queue<MicroService> getEventSub(Class<? extends Event<?>> type) {
		return eventSub.get(type);
	}

	public Future<?> getEventFuture(Event<?> event) {
		return eventFutureMap.get(event);
	}

	public ConcurrentHashMap<Class<? extends Event<?>>, Queue<MicroService>> getEventSub() {
		return eventSub;
	}

}