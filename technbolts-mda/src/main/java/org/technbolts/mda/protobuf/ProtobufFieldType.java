package org.technbolts.mda.protobuf;

public enum ProtobufFieldType {
    Int("int32"),
    Long("int64"),
    Float("float"),
    Double("double"),
    Bool("bool"),
    String("string"),
    Bytes("bytes"),
    Auto("<auto:not_resolved>"),
    Message("<message:not_resolved>");
    
    public final String pbuf;

    private ProtobufFieldType(String pbuf) {
        this.pbuf = pbuf;
    }
}
