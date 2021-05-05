package bgu.spl.mics.application.passiveObjects;


import java.util.LinkedList;
import java.util.List;

/**
 * Passive object representing the resource manager.
 * <p>
 * This class must be implemented as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class Ewoks {

    /**
     * This private class forces the ClassLoader to load it once in the  whole program,
     * using lazy initialization, which means that the class {@link Ewoks}
     * can have only one instance in the whole program (application).
     */
    private static class EwoksHolder {
        private final static Ewoks instance = new Ewoks();
    }

    private final List<Ewok> ewoksList = new LinkedList<>();

    /**
     * Instantiates {@link Ewoks} if it is not instantiated yet.
     * <p>
     * @return  {@link Ewoks} instance of the class (singleton).
     */
    public static Ewoks getInstance(){
        return EwoksHolder.instance;
    }


    /**
     * Builds the ewoksList which contains all ewoks that can be used.
     * <p>
     * @param ewoksNum  number of ewoks needed to be created
     */
    public void buildEwoksList(int ewoksNum){
        for(int i=1 ; i<=ewoksNum ; i++){
            ewoksList.add(new Ewok(i));
        }
    }

    public void acquireEwoks(List<Integer> ewoks){
        /* iterate over the ewoksList field, for each ewok if it is in the ewoks-list-parameter,
         * then that ewok needs to be acquired, if it is not available, wait for it until it
         * becomes available, acquire it and continue to the others (the wait
         * notify synchronization is implemented in the Ewok class).
         * iterating over all existing ewoks and checking for each of them if we need them
         * gives an order upon acquiring (meaning synchronizing in the Ewok class)
         * and thus it prevents DeadLocks.
         */
        for (Ewok ewok : ewoksList) {
            if (ewoks.contains(ewok.serialNumber)) {
                ewok.acquire();
            }
        }
    }

    public void releaseEwoks(List<Integer> ewoks){
        for (Ewok ewok : ewoksList) {
            if (ewoks.contains(ewok.serialNumber)) {
                ewok.release();
            }
        }
    }
}
