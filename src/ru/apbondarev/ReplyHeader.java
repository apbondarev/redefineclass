package ru.apbondarev;

public class ReplyHeader {
    private final int dataLength;
    private final int id;
    private final byte flags;
    private final ErrorCode errorCode;

    public ReplyHeader(int dataLength, int id, byte flags, short errorCode) {
        this.dataLength = dataLength;
        this.id = id;
        this.flags = flags;
        this.errorCode = ErrorCode.valueOf(errorCode);
    }

    public int getDataLength() {
        return dataLength;
    }

    public int getId() {
        return id;
    }

    public byte getFlags() {
        return flags;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
