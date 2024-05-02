package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.*;

public class AtomicWriteOnceReferenceTest {

    Factory factory = new Factory();
    private static Integer nbThread;

    @BeforeTest
    void setUp() throws ClassNotFoundException {
        factory = new Factory();
        nbThread = Runtime.getRuntime().availableProcessors();

        setTest();
    }

    @Test
    private void setTest() throws ClassNotFoundException {

        AtomicWriteOnceReference<Integer> reference = (AtomicWriteOnceReference<Integer>) factory.newObject("AtomicWriteOnceReference");

        AtomicReference<Integer> atomicReference = (AtomicReference<Integer>) factory.newObject("AtomicReference");

        reference.set(10);
        atomicReference.set(10);

        assertEquals(reference.get(), atomicReference.get(),"Failed setting the reference");
    }
}