package bgu.spl.mics.application.passiveObjects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EwokTest {

    private Ewok ewok;

    @BeforeEach
    void setUp() {
        ewok = new Ewok();
    }

    @Test
    void acquire() {
        int preSerialNum = ewok.serialNumber;
        assertTrue(ewok.available);
        ewok.acquire();
        assertFalse(ewok.available);
        assertEquals(preSerialNum, ewok.serialNumber);

    }

    @Test
    void release() {
        int preSerialNum = ewok.serialNumber;
        assertTrue(ewok.available);
        ewok.acquire();
        assertFalse(ewok.available);
        ewok.release();
        assertTrue(ewok.available);
        assertEquals(preSerialNum, ewok.serialNumber);
    }
}