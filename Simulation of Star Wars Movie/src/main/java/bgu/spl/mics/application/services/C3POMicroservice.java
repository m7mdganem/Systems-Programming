package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.passiveObjects.Ewoks;

/**
 * C3POMicroservices is in charge of the handling {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class C3POMicroservice extends MicroService {

    private final Ewoks ewoks = Ewoks.getInstance();

    public C3POMicroservice() {
        super("C3PO");
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminationBroadcast.class, x -> {
            terminate();
            diary.setC3POTerminate();//set the termination time of C3PO to the diary
        });
        subscribeEvent(AttackEvent.class , attackEvent -> {
            ewoks.acquireEwoks(attackEvent.getSerialNumbers());//acquire required Ewoks
            //simulate attack by sleeping
            try {
                Thread.sleep(attackEvent.getAttackDuration());
            }catch (InterruptedException e){
            }
            ewoks.releaseEwoks(attackEvent.getSerialNumbers());//release acquired Ewoks
            complete(attackEvent, true);//mark event as completed
            diary.incrementTotalAttacks();//add the number of done attacks in the diary
            /* set the finish time of C3PO
             * every attack we set the new finish time, thus in the last attack will be the
             * last attack and then we will get the right C3POFinish time
             */
            diary.setC3POFinish();
        });
    }
}
