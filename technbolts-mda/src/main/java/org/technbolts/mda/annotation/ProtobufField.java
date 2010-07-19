package org.technbolts.mda.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ProtobufField {
    public enum Mode {
        Optional,
        Repeated,
        Required
    }
    Mode mode()   default Mode.Optional;
    int order()   default -1;
    String name() default "";
}
