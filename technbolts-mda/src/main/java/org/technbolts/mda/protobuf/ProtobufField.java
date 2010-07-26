package org.technbolts.mda.protobuf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ProtobufField {
    
    ProtobufFieldClassifier classifier() default ProtobufFieldClassifier.Auto;
    int order()   default -1;
    String name() default "";
    ProtobufFieldType fieldType() default ProtobufFieldType.Auto;
    String defaultValue() default "";
}