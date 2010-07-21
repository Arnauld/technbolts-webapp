package org.technbolts.mda.protobuf;

public enum ProtobufFieldClassifier {
    Optional("optional"),
    Repeated("repeated"),
    Required("required"),
    Auto("<auto:not_resolved>");
    
    public final String pbuf;

    private ProtobufFieldClassifier(String pbuf) {
        this.pbuf = pbuf;
    }
}
