package eu.cloudbutton.dobj.types;

public class CounterFactory {

    public Counter createdegradablecounter() { return new DegradableCounter(); }
    public Counter createjavacounter() { return new JavaCounter(); }
}
