package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.FactoryIndice;
import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.*;

public class AtomicWriteOnceReferenceTest {

    Factory factory = new Factory();
    private FactoryIndice factoryIndice;
    private static Integer nbThread;

    @BeforeTest
    void setUp() {
        factory = new Factory();
        nbThread = Runtime.getRuntime().availableProcessors();
//        nbThread = 1;
        factoryIndice = new FactoryIndice(nbThread);
    }

    @Test
    private void setTest() throws ClassNotFoundException {

        AtomicWriteOnceReference<Integer> reference = (AtomicWriteOnceReference<Integer>) factory.createObject("AtomicWriteOnceReference", factoryIndice);

        AtomicReference<Integer> atomicReference = (AtomicReference<Integer>) factory.createObject("AtomicReference", factoryIndice);

        reference.set(10);
        atomicReference.set(10);

        assertEquals(reference.get(), atomicReference.get(),"Failed setting the reference");
    }
}