package bgu.spl.mics;

import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.services.R2D2Microservice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageBusImplTest {

    private MessageBus messageBus;

    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getInstance();
    }

    @Test
    void complete() {
        MicroService R2D2 = new R2D2Microservice(1000); // initialize a microService
        messageBus.register(R2D2);//register microservice to messageBus
        Event<Boolean> event = new AttackEvent();//create a new attack event
        Boolean result = true;
        messageBus.subscribeEvent((Class<? extends Event<String>>) event.getClass(), R2D2);
        Future<Boolean> future = messageBus.sendEvent(event);
        try {
            //event is not completed yet -> future is not resolved yet -> future is not done
            assertFalse(future.isDone());
        } catch (NullPointerException e) {
            assertFalse(true);
        }
        messageBus.complete(event, result);
        try {
            //event is completed -> future is resolved -> future is done
            assertTrue(future.isDone());
        }catch(NullPointerException e){
            assertFalse(true);
        }
        /* since messageBus is a singleton, thus we have to unregister so that we
         * dont the current state does not affect other tests.
         */
        messageBus.unregister(R2D2);
    }

    @Test
    void sendBroadcast() {
        /*since we first subscribe to a broadcast and then send a broadcast message
         *in this test we test both methods - subscribeBroadcast and sendBroadcast
         */
        MicroService R2D2 = new R2D2Microservice(1000); // initialize a microService
        MicroService R2D2Junior = new R2D2Microservice(100); // initialize a microService
        messageBus.register(R2D2); //register microService to messageBus
        messageBus.register(R2D2Junior); //register microService to messageBus
		Broadcast terminationBroadcast = new TerminationBroadcast(); //create a new Broadcast
		messageBus.subscribeBroadcast((Class<? extends Broadcast>) terminationBroadcast.getClass(), R2D2);
        messageBus.subscribeBroadcast((Class<? extends Broadcast>) terminationBroadcast.getClass(), R2D2Junior);
        messageBus.sendBroadcast(terminationBroadcast);
        Message message = null;
        try{
            message = messageBus.awaitMessage(R2D2);
        } catch(InterruptedException e){
            assertFalse(true);
        }
        assertNotNull(message);
        try{
            message = messageBus.awaitMessage(R2D2Junior);
        } catch(InterruptedException e){
            assertFalse(true);
        }
        assertNotNull(message);
        /* since messageBus is a singleton, thus we have to unregister so that we
         * dont the current state does not affect other tests.
         */
        messageBus.unregister(R2D2);
        messageBus.unregister(R2D2Junior);
    }

    @Test
    void sendEvent() {
        /*since we first subscribe to an event and then send an event message
         *in this test we test both methods - subscribeEvent and sendEvent
         *also we register the microservice and then unregister it, then we also
		 *check those two methods
		 */
        MicroService R2D2 = new R2D2Microservice(1000); // initialize a microService
        messageBus.register(R2D2);
        Event<Boolean> attackEvent = new AttackEvent(); //create a new attack event
        Future<Boolean> future = messageBus.sendEvent(attackEvent);
        assertNull(future);//when no microservice is subscribed we expect that sendEvent returns null
        messageBus.subscribeEvent((Class<? extends Event<String>>) attackEvent.getClass(), R2D2);
        future = messageBus.sendEvent(attackEvent);
        assertNotNull(future);//when there are subscribers we expect that sendEvent does not return null
		messageBus.unregister(R2D2);
		try{//we unregistered R2D2 then awaitMessage should throw an exception
            messageBus.awaitMessage(R2D2);
        } catch(InterruptedException e){
        }catch(NullPointerException e ){}
    }

    @Test
    void awaitMessage() {
        MicroService R2D2 = new R2D2Microservice(1000);
        messageBus.register(R2D2);
        Event<Boolean> attackEvent = new AttackEvent();
        messageBus.subscribeEvent((Class<? extends Event<Boolean>>) attackEvent.getClass() , R2D2);
        messageBus.sendEvent(attackEvent);
        Message message = null;
        try{
            message = messageBus.awaitMessage(R2D2);
        } catch(InterruptedException e){
            assertFalse(true);
        }
        assertNotNull(message);
        messageBus.unregister(R2D2);
    }
}
