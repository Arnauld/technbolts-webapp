package org.technbolts.mda;

public interface Converter<F,T> {
    Class<T> destinationType();
    T convert(F value);
}
