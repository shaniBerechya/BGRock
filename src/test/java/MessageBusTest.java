import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.Transient;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleBroadcastListenerService;
import bgu.spl.mics.example.services.ExampleEventHandlerService;
import bgu.spl.mics.example.services.ExampleMessageSenderService;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Future;
import bgu.spl.mics.Message;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.Test;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.example.messages.ExampleEvent;

public class MessageBusTest {
    @Test
public void subscribeEventTest() {
    // Create an instance of MessageBusImpl (singleton)
    MessageBusImpl messageBus = MessageBusImpl.getInstance();

    // Create a handler service and an event
    ExampleEventHandlerService handler1 = new ExampleEventHandlerService("Handler1", new String[]{"2"});
    ExampleEvent event = new ExampleEvent("TestEvent");

    // Register the handler to the MessageBus
    messageBus.register(handler1);

    // Subscribe the handler to the ExampleEvent type
    messageBus.subscribeEvent(event.getClass(), handler1);

    // Get the subscribers queue for the event type
    BlockingQueue<MicroService> subscribers = messageBus.getEventSub(event.getClass());

    // Ensure the subscribers queue is not null
    assertNotNull(subscribers, "The subscribers queue for the event should not be null.");

    // Ensure the subscribers queue contains exactly 1 handler
    assertEquals(1, subscribers.size(), "The subscribers queue should contain exactly 1 handler.");

    // Ensure the specific handler is in the subscribers queue
    assertTrue(subscribers.contains(handler1), "The subscribers queue should contain Handler1.");
}


    @Test
    public void subscribeBroadcastTest() {
        MessageBusImpl messageBus = MessageBusImpl.getInstance();
    
        // Create two broadcast listeners
        ExampleBroadcastListenerService listener1 = new ExampleBroadcastListenerService("Listener1", new String[]{"5"});
        ExampleBroadcastListenerService listener2 = new ExampleBroadcastListenerService("Listener2", new String[]{"3"});
    
        // Register the listeners
        messageBus.register(listener1);
        messageBus.register(listener2);
    
        // Subscribe both listeners to ExampleBroadcast
        messageBus.subscribeBroadcast(ExampleBroadcast.class, listener1);
        messageBus.subscribeBroadcast(ExampleBroadcast.class, listener2);
    
        // Get the list of subscribers for the broadcast type
        LinkedList<MicroService> subscribers = messageBus.getBrodSub(ExampleBroadcast.class);
    
        // Ensure the subscribers list is not null
        assertNotNull(subscribers, "The subscribers list should not be null.");
    
        // Ensure the subscribers list contains exactly 2 listeners
        assertEquals(2, subscribers.size(), "The subscribers list should contain exactly 2 listeners.");
        
        // Ensure the specific handler is in the subscribers queue
        assertTrue(subscribers.contains(listener1), "The subscribers list should contain listener1.");
        assertTrue(subscribers.contains(listener2), "The subscribers list should contain listener2.");
    }
    

    @Test
    public void completeTest() {
        MessageBusImpl messageBus = MessageBusImpl.getInstance();

        // Setting up an event and microservice
        ExampleEvent event = new ExampleEvent("TestSender");
        MicroService handler = new ExampleBroadcastListenerService("Handler", new String[]{"1"});
        messageBus.register(handler);

        // Sending event and obtaining future
        Future<String> future = (Future<String>) messageBus.sendEvent(event);

        // Completing the event
        messageBus.complete(event, "Completed");

        // Verifying future is resolved
        assertTrue(future.isDone());
        assertEquals("Completed", future.get()); // Ensure correct resolution

        //Verifying future is not in the future-event hash
        assertNull(messageBus.getFuture(event));
    }

    @Test
    public void sendBroadcastTest() throws InterruptedException {
    MessageBusImpl messageBus = MessageBusImpl.getInstance();

    // Setting up microservices and broadcast
    ExampleBroadcastListenerService listener1 = new ExampleBroadcastListenerService("Listener1", new String[]{"2"});
    ExampleBroadcastListenerService listener2 = new ExampleBroadcastListenerService("Listener2", new String[]{"3"});
    ExampleBroadcast broadcast = new ExampleBroadcast("TestBroadcast");

    messageBus.register(listener1);
    messageBus.register(listener2);
    messageBus.subscribeBroadcast(ExampleBroadcast.class, listener1);
    messageBus.subscribeBroadcast(ExampleBroadcast.class, listener2);

    // Sending broadcast
    messageBus.sendBroadcast(broadcast);

    // Verify that the broadcast was sent correctly
    LinkedList<Message> queue1 = messageBus.getMessageQueue(listener1);
    LinkedList<Message> queue2 = messageBus.getMessageQueue(listener2);

    assertTrue(queue1.contains(broadcast));
    assertTrue(queue2.contains(broadcast));
}


@Test
public void sendEventTest() throws InterruptedException {
    MessageBusImpl messageBus = MessageBusImpl.getInstance();

    // Setting up microservices and event
    ExampleEvent event = new ExampleEvent("TestEvent");
    MicroService handler = new ExampleBroadcastListenerService("Handler", new String[]{"1"});

    // Register but do not subscribe the handler to the event
    messageBus.register(handler);

    // Sending event
    Future<String> future = messageBus.sendEvent(event);

    // Verify the event is received by the handler
    LinkedList<Message> queue1 = messageBus.getMessageQueue(handler);
    assertTrue(queue1.contains(event));
    assertEquals(future, messageBus.getFuture(event));
}

    @Test
    public void registerMicroServiceTest() {
        MessageBusImpl messageBus = MessageBusImpl.getInstance();

        // Register an event and verify queue creation
        MicroService eventHandler = new ExampleEventHandlerService("EventHandler", new String[]{"5"});
        messageBus.register(eventHandler);
        assertNotNull(messageBus.getMessageQueue(eventHandler));

        // Register a message and verify queue creation
        MicroService messageSender = new ExampleMessageSenderService("MessageSender", new String[]{"event"});
        messageBus.register(messageSender);
        assertNotNull(messageBus.getMessageQueue(messageSender));

        // Test registering multiple microservices and ensuring queue
        assertNotSame(messageBus.getMessageQueue(messageSender), messageBus.getMessageQueue(eventHandler));
    }

    @Test
    public void testUnregister() {
        // Get the singleton instance of MessageBusImpl
        MessageBusImpl messageBus = MessageBusImpl.getInstance();
    
        // Create microservices
        MicroService broadcastListener = new ExampleBroadcastListenerService("BroadcastListener", new String[]{"1"});
        MicroService eventHandler = new ExampleEventHandlerService("EventHandler", new String[]{"1"});
        MicroService sender = new ExampleMessageSenderService("Sender", new String[]{"event"});
    
        // Register the microservices
        messageBus.register(broadcastListener);
        messageBus.register(eventHandler);
        messageBus.register(sender);
    
        // Subscribe the broadcast listener to ExampleBroadcast
        messageBus.subscribeBroadcast(ExampleBroadcast.class, broadcastListener);
    
        // Subscribe the event handler to ExampleEvent
        messageBus.subscribeEvent(ExampleEvent.class, eventHandler);
    
        // Send an ExampleBroadcast
        ExampleBroadcast broadcast = new ExampleBroadcast("Sender");
        messageBus.sendBroadcast(broadcast);
    
        // Send an ExampleEvent
        ExampleEvent event = new ExampleEvent("Sender");
        Future<String> future = messageBus.sendEvent(event);
    
        // Unregister the broadcast listener and verify removal
        messageBus.unregister(broadcastListener);
        assertFalse(messageBus.getBrodSub(ExampleBroadcast.class).contains(broadcastListener), "BroadcastListener should be removed from broadcast subscriptions.");
        assertNull(messageBus.getMessageQueue(broadcastListener), "BroadcastListener queue should be removed.");
        assertNull(messageBus.getMicroToBrod(broadcastListener), "BroadcastListener should be removed from microToBrod.");
    
        // Unregister the event handler and verify removal
        messageBus.unregister(eventHandler);
        assertFalse(messageBus.getEventSub(ExampleEvent.class).contains(eventHandler), "EventHandler should be removed from event subscriptions.");
        assertNull(messageBus.getMessageQueue(eventHandler), "EventHandler queue should be removed.");
        assertNotNull(messageBus.getMicroToEvent(sender), "Sender should still exist in microToEvent (if applicable).");
    
        // Verify that the sender is still registered
        assertNotNull(messageBus.getMessageQueue(sender), "Sender queue should still exist.");
    }




    @Test
    public void testAwaitMessageWaitsForMessage() throws InterruptedException {
    MessageBusImpl messageBus = MessageBusImpl.getInstance();
    MicroService broadcastListener = new ExampleBroadcastListenerService("Listener1", new String[]{"1"});
    MicroService broadcastSender = new ExampleMessageSenderService("Listener1", new String[]{"broadcast"});

    messageBus.register(broadcastListener);
    messageBus.register(broadcastSender);
    
    Thread listener = new Thread(( -> ))
}



 }