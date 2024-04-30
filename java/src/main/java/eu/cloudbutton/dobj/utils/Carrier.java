package eu.cloudbutton.dobj.utils;

import jdk.internal.vm.annotation.ForceInline;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Carrier {

    public static Method currentCarrierThread;

    static {
        try {
            currentCarrierThread = Thread.class.getDeclaredMethod("currentCarrierThread");
            currentCarrierThread.setAccessible(true);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @ForceInline
    public static final int carrierID() {
        try {
            long id = ((Thread) currentCarrierThread.invoke(null)).threadId();
            return (int) id;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
