package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.*;

public class AtomicWriteOnceReferenceTest {

    Factory factory = new Factory();

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }

    @Test
    private void setTest() throws ClassNotFoundException {

        AtomicWriteOnceReference<Integer> reference = (AtomicWriteOnceReference<Integer>) factory.createObject("AtomicWriteOnceReference", 1);

        AtomicReference<Integer> atomicReference = (AtomicReference<Integer>) factory.createObject("AtomicReference", 1);

        reference.set(10);
        atomicReference.set(10);

        assertEquals(reference.get(), atomicReference.get(),"Failed setting the reference");
    }
}