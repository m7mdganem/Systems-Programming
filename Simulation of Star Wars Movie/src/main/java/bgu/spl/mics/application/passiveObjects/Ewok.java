package bgu.spl.mics.application.passiveObjects;

/**
 * Passive data-object representing a forest creature summoned when HanSolo and C3PO receive AttackEvents.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class Ewok {
	int serialNumber;
	boolean available = true;

    /**
     * Empty constructor, used only for unit tests
     */
	public Ewok(){
	    serialNumber = 0;
    }

    /**
     * constructor
     * <P>
     * @param serialNumber  the serialNumber of the {@link Ewok}
     */
	public Ewok(int serialNumber){
	    this.serialNumber = serialNumber;
    }
  
    /**
     * Acquires an Ewok.
     * if Ewok not available, wait until it is released by other thread.
     */
    public synchronized void acquire() {
        while(!available){
            try {
                wait();
            }catch (InterruptedException e){
            }
        }
        available = false;
    }

    /**
     * release an Ewok
     * notify all waiting threads that this Ewok is now available.
     */
    public synchronized void release() {
        available = true;
        notifyAll();
    }

    /**
     * Retrieves if the Ewok is available.
     * <p>
     * @return if the Ewok is available.
     */
    public synchronized boolean isAvailable() {
        return available;
    }
}
