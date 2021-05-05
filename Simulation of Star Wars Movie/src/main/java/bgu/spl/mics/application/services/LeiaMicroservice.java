package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Attack;

/**
 * LeiaMicroservices Initialized with Attack objects, and sends them as  {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LeiaMicroservice extends MicroService {
	private final Attack[] attacks;

    public LeiaMicroservice(Attack[] attacks) {
        super("Leia");
		this.attacks = attacks;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminationBroadcast.class, x -> {
        	terminate();
        	diary.setLeiaTerminate();//set the termination time of Leia to the diary
		});
		/* According to the instructions that was given in the forum via Vaknin
		 * sleep for a while before stating giving messages will solve the problem
		 * of stating sending messages while C3PO and HanSolo is not yet subscribed.
		 * Question: "Leia sending attack events" in the forum.
		 */
		try {
			Thread.sleep(1000);
		}catch(InterruptedException e) {
		}
		for(Attack attack : attacks){
			AttackEvent attackEvent = new AttackEvent(attack);
			sendEvent(attackEvent);
		}
		/* After sending all attacks, we have to wait until all attacks are done
		 * and for each attack we have a Future object associated with it, thus
		 * for each such future, call the get() method, this method is blocking,
		 * meaning that each call will block us from proceeding until the appropriate
		 * event (attack) is resolved (done).
		 */
		for (Future future : futures.values()){
			future.get();
		}
		//when reaching here, all attacks are done, thus now we send the deactivationEvent to R2D2
		DeactivationEvent DE = new DeactivationEvent();
		sendEvent(DE);
		/* We call the method get() to the future associated to the deactivationEvent
		 * that was sent to R2D2 so that we dont proceed until the deactivation is done.
		 */
		futures.get(DE).get();
		//when reaching here, the DeactivationEvent is done, thus now we send the BombDestroyerEvent to Lando
		BombDestroyerEvent BDE  = new BombDestroyerEvent();
		sendEvent(BDE);
		/* We call the method get() to the future associated to the BombDestroyerEvent
		 * that was sent to Lando so that we dont proceed until the Bomb Destroying is done.
		 */
		futures.get(BDE).get();
		//when reaching here, the BombDestroyerEvent is done, thus now we send the TerminationBroadcast to all microservices
		TerminationBroadcast TB = new TerminationBroadcast();
		sendBroadcast(TB);
    }
}
