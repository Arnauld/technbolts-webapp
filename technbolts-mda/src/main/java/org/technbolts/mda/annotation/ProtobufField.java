package org.technbolts.mda.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ProtobufField {
    public enum Mode {
        Optional("optional"),
        Repeated("repeated"),
        Required("required");
        public final String pbuf;
        private Mode(String pbuf) { this.pbuf = pbuf; }
    }
    public enum Type {
        Int("int32"),
        Long("int64"),
        Float("float"),
        Double("double"),
        Bool("bool"),
        String("string"),
        Bytes("bytes"),
        Auto("n/a");
        public final String pbuf;
        private Type(String pbuf) { this.pbuf = pbuf; }
    }
    Mode mode()   default Mode.Optional;
    int order()   default -1;
    String name() default "";
    Type fieldType() default Type.Auto;
}
