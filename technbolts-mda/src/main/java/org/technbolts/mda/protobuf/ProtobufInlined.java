package org.technbolts.mda.protobuf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ProtobufInlined {
    ProtobufFieldType fieldType() default ProtobufFieldType.Auto;
}
