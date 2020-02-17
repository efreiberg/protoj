package dev.freemountain.protoj.internal;

public enum WireType {
    VARINT(0),
    FIXED_32(5),
    FIXED_64(1),
    LENGTH_DELIMITED(2);

    private int wireTypeId;

    WireType(int wireTypeId) {
        this.wireTypeId = wireTypeId;
    }

    public int getWireTypeId() {
        return this.wireTypeId;
    }
}
