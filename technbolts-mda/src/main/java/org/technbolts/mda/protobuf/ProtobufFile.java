package org.technbolts.mda.protobuf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ProtobufFile {
    String name();
    String protoFileName() default "";
    String protoPackage() default "";
    String javaPackage() default "";
    String javaOuterClassName() default "";
    boolean optimizedForSpeed() default true;
}
