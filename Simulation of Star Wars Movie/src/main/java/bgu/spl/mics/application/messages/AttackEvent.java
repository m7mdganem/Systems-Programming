package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Attack;

import java.util.List;

public class AttackEvent implements Event<Boolean> {

    private List<Integer> serials;
    private int duration;

    /**
     * Empty constructor, used only for testing purposes.
     */
    public AttackEvent(){
    }

    /**
     * constructor
     * <p>
     * @param attack the attack that this attackEvent represents.
     */
    public AttackEvent(Attack attack){
        serials = attack.getSerialNumbers();
        duration = attack.getAttackDuration();
    }

    /**
     * @return  required Ewoks serial-numbers for the attack
     */
    public List<Integer> getSerialNumbers(){
        return serials;
    }

    /**
     * @return  required attack duration
     */
    public int getAttackDuration(){
        return  duration;
    }
}
