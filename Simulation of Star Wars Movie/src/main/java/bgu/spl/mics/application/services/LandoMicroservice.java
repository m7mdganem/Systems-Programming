package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;

/**
 * LandoMicroservice
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LandoMicroservice  extends MicroService {

    private final long bombingDuration;

    public LandoMicroservice(long duration) {
        super("Lando");
        bombingDuration = duration;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminationBroadcast.class, x -> {
            terminate();
            diary.setLandoTerminate();//set the termination time of Lando to the diary
        });
        subscribeEvent(BombDestroyerEvent.class, x ->{
            //simulate bombing by sleeping
            try {
                Thread.sleep(bombingDuration);
            }catch (InterruptedException e){
            }
            complete(x,true);//mark the deactivationEvent as completed
        });
    }
}
