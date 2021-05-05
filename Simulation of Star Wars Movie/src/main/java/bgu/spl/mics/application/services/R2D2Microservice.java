package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;


/**
 * R2D2Microservices is in charge of the handling {@link DeactivationEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link DeactivationEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class R2D2Microservice extends MicroService {

    private final long deactivationDuration;

    public R2D2Microservice(long duration) {
        super("R2D2");
        deactivationDuration = duration;
    }


    @Override
    protected void initialize() {
        subscribeBroadcast(TerminationBroadcast.class, x -> {
            terminate();
            diary.setR2D2Terminate(); //set the termination time of R2D2 to the diary
        });
        subscribeEvent(DeactivationEvent.class, x ->{
            //simulate deactivation by sleeping
            try{
                Thread.sleep(deactivationDuration);
            }catch (InterruptedException e){
            }
            complete(x, true);//mark the deactivationEvent as completed
            diary.setR2D2Deactivate();//set the deactivation time to the diary
        });

    }
}
