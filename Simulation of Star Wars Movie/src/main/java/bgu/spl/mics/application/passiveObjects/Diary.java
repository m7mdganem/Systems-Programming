package bgu.spl.mics.application.passiveObjects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive data-object representing a Diary - in which the flow of the battle is recorded.
 * We are going to compare your recordings with the expected recordings, and make sure that your output makes sense.
 * <p>
 * Do not add to this class nothing but a single constructor, getters and setters.
 */
public class Diary {
    private static class DiaryHolder {
        private final static Diary instance = new Diary();
    }

    private final AtomicInteger totalAttacks = new AtomicInteger(0);//represent the total number of attacks
    private long HanSoloFinish = 0;//represents the time HanSolo finishes processing all his attacks.
    private long C3POFinish = 0;//represents the time C3PO finishes processing all his attacks.
    private long R2D2Deactivate = 0;//represents the time R2D2 finishes deactivating the shield.
    private long LeiaTerminate = 0;//represents the time Leia terminates.
    private long HanSoloTerminate = 0;//represents the time HanSolo terminates.
    private long C3POTerminate = 0;//represents the time C3PO terminates.
    private long R2D2Terminate = 0;//represents the time R2D2 terminates.
    private long LandoTerminate = 0;//represents the time Lando terminates.

    /**
     * Instantiates {@link Diary} if it is not instantiated yet.
     * <p>
     * @return  {@link Diary} instance of the class (singleton).
     */
    public static Diary getInstance(){
        return DiaryHolder.instance;
    }

    /**
     * increases the number of total attacks by 1.
     */
    public void incrementTotalAttacks(){
        totalAttacks.incrementAndGet();//Atomic operation
    }

    /**
     * Set the time HanSolo finishes applying all his attacks.
     */
    public void setHanSoloFinish(){
        HanSoloFinish = System.currentTimeMillis();
    }

    /**
     * Set the time C3PO finishes applying all his attacks.
     */
    public void setC3POFinish(){
        C3POFinish = System.currentTimeMillis();
    }

    /**
     * Set the time R2D2 finishes deactivating the shield.
     */
    public void setR2D2Deactivate(){
        R2D2Deactivate = System.currentTimeMillis();
    }

    /**
     * Set the time Leia Terminates.
     */
    public  void setLeiaTerminate(){
        LeiaTerminate = System.currentTimeMillis();
    }
    /**
     * Set the time HanSolo Terminates.
     */
    public void setHanSoloTerminate(){
        HanSoloTerminate = System.currentTimeMillis();
    }

    /**
     * Set the time C3PO Terminates.
     */
    public void setC3POTerminate(){
        C3POTerminate = System.currentTimeMillis();
    }

    /**
     * Set the time R2D2 Terminates.
     */
    public void setR2D2Terminate(){
        R2D2Terminate = System.currentTimeMillis();
    }

    /**
     * Set the time Lando Terminates.
     */
    public void setLandoTerminate(){
        LandoTerminate = System.currentTimeMillis();
    }
}
