package eu.cloudbutton.dobj.utils;

import jdk.internal.misc.CarrierThreadLocal;

import java.util.Objects;
import java.util.function.Supplier;

public class CarrierThreadLocalWithInitial<T> extends CarrierThreadLocal<T> {

    private final Supplier<? extends T> supplier;

    CarrierThreadLocalWithInitial(Supplier<? extends T> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    @Override
    protected T initialValue() {
        return supplier.get();
    }

}
