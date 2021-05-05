package bgu.spl.mics;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	/**
	 * This class forces the ClassLoader to load it once in the  whole program,
	 * using lazy initialization, which causes the class {@link MessageBus}
	 * to have only one instance in the whole program (application).
	 */
	private static class MessageBusHolder {
		private final static MessageBusImpl instance = new MessageBusImpl();
	}

	/*
	 * The {@code eventSubscribers} field is a hash-map which maps each event
	 * to a collection which contains all subscribers to that event.
	 * We define singleton's field as final so that they can be initialized only once.
	 */
	private final ConcurrentHashMap<Class<? extends Event<?>> ,ConcurrentLinkedQueue<MicroService>> eventSubscribers
			= new ConcurrentHashMap<>();
	/*
	 * The {@code broadcastSubscribers} field is a hash-map which maps each broadcast
	 * to a list which contains all subscribers to that broadcast.
	 * We define singleton's field as final so that they can be initialized only once.
	 */
	private final ConcurrentHashMap<Class<? extends  Broadcast>, List<MicroService>> broadcastSubscribers
			= new ConcurrentHashMap<>();
	/*
	 * The {@code microServicesMessagesQueues} field is a hash map which maps each Microservice which
	 * is registered to this MessageBus to a queue of all its messages that needs to be processed.
	 * We define singleton's field as final so that they can be initialized only once.
	 */
	private final ConcurrentHashMap<MicroService, Queue<Message>> microServicesMessagesQueues
			= new ConcurrentHashMap<>();
	/*
	 * The {@code futureMap} field is a hash map which maps each Event that
	 * we get from calling the sendEvent method to the future object associated to it.
	 * We define singleton's field as final so that they can be initialized only once.
	 */
	private final ConcurrentHashMap<Event<?>, Future<?>> futureMap = new ConcurrentHashMap<>();


	/**
	 * Instantiates {@link MessageBusImpl} if it is not instantiated yet.
	 * <p>
	 * @return  {@link MessageBus} instance of the class (singleton).
	 */
	public static MessageBusImpl getInstance(){
		return MessageBusHolder.instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		eventSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());//Atomic operation
		ConcurrentLinkedQueue<MicroService> subscribersQueue = eventSubscribers.get(type);
		synchronized(subscribersQueue){
			if(!subscribersQueue.contains(m))
				subscribersQueue.add(m);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		broadcastSubscribers.putIfAbsent(type, new LinkedList<>());//Atomic operation
		/*
		 * putIfAbsent insures that at this point broadcastSubscribers.get(type)!= null
		 * (because if there was not any value associated with the (type) key it adds a new empty linked-list
		 */
		List<MicroService> subscribersList = broadcastSubscribers.get(type);
		synchronized (subscribersList){
			if(!subscribersList.contains(m))
				subscribersList.add(m);
		}
    }

	@Override @SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		Future future = futureMap.get(e);
		future.resolve(result);
		
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		//get the micro-services queue which are subscribed to given broadcast
		List<MicroService> subscribersList = broadcastSubscribers.get(b.getClass());
		/*
		 * synchronizing on that List insures that no other threads modifies it while in block.
		 * this is necessary since we are iterating over all its elements.
		 */
		synchronized (subscribersList){
			for(MicroService m : subscribersList) {//go over all microservices subscribed to this broadcast
				Queue<Message> messagesQueue = microServicesMessagesQueues.get(m);//get its messages queue
				synchronized (messagesQueue) {
					messagesQueue.add(b);//add the broadcast to the appropriate messages queue
					messagesQueue.notifyAll();//notify threads who are waiting for a message to be added
				}
			}
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		//get the micro-services queue which are subscribed to given event
		ConcurrentLinkedQueue<MicroService> subscribersQueue = eventSubscribers.get(e.getClass());
		/*
		 * synchronizing on that queue insures that no other threads modifies it while in block.
		 * this is necessary since we are going to modify the queue (remove and add objects from it).
		*/
		if(subscribersQueue!=null) {
			synchronized (subscribersQueue) {
				/*
				 * We implement the round-robin manner by adding the event to the first micro-service
				 * in the queue (the one in the head of the queue), then removing that micro-service
				 * from the head of the queue and adding it to the tail.
				 */
				MicroService roundRobinNext = subscribersQueue.poll();//get the first micro-service and remove it from head
				Queue<Message> messagesQueue = microServicesMessagesQueues.get(roundRobinNext);//get its messages queue
				Future<T> future = new Future<>();
				futureMap.putIfAbsent(e, future);
				synchronized (messagesQueue) {
					messagesQueue.add(e);//add the event to the appropriate messages queue
					messagesQueue.notifyAll();//notify threads who are waiting for a message to be added
				}
				subscribersQueue.add(roundRobinNext);//return the microservice to the tail of the queue
				return future;//return the future associated with the event
			}
		}
		return null;
	}

	@Override
	public void register(MicroService m) {
		/*
		 * we know that each micro-service is registered only once during all the application
		 * lifetime, thus when calling register method we know that there is no other instance
		 * of the same micro-service in this hashmap, but we use putIfAbsent method instead of
		 * put method because according to ConcurrentHashMap API this method is implemented as
		 * atomic operation, thus it may prevent wrong usage or wrong calling of the method.
		 */
		microServicesMessagesQueues.putIfAbsent(m, new LinkedList<>());
	}

	@Override
	public synchronized void unregister(MicroService m) {
		//remove all remaining messages from m's messages-queue
		while(!microServicesMessagesQueues.get(m).isEmpty()){
			Message message = microServicesMessagesQueues.get(m).poll();
			if(message instanceof Event)
				futureMap.remove(message);
		}
		//remove m's messages-queue
		microServicesMessagesQueues.remove(m);
		/* for each event, if m is a subscriber to that event then remove m from the subscribers queue,
		 * if m is the only subscriber left in that event, remove the event from the map.
		 */
		for(Class<?> key : eventSubscribers.keySet()){
			if(eventSubscribers.get(key).contains(m))
				eventSubscribers.get(key).remove(m);
			if(eventSubscribers.get(key).isEmpty())
				eventSubscribers.remove(key);
		}
		/*
		 * for each broadcast, if m is a subscriber to that broadcast then remove m from the subscribers queue,
		 * if m is the only subscriber left in that broadcast, remove the broadcast from the map.
		 */
		for(Class<?> key : broadcastSubscribers.keySet()){
			synchronized (broadcastSubscribers.get(key)) {
				if (broadcastSubscribers.get(key).contains(m))
					broadcastSubscribers.get(key).remove(m);
				if (broadcastSubscribers.get(key).isEmpty())
					broadcastSubscribers.remove(key);
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		//get the messages queue which is associated to the microservice m.
		Queue<Message> messagesQueue = microServicesMessagesQueues.get(m);
		synchronized (messagesQueue){
			while(messagesQueue.isEmpty()) {
				//if there are no messages in the queue wait until another threads sends some message
				try {
					messagesQueue.wait();
				} catch (InterruptedException e) {
					throw e;
				}
			}
			//when reaching here, there is a message in the queue, remove it from the queue and return it.
			return messagesQueue.poll();
		}
	}
}
