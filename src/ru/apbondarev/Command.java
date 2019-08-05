package ru.apbondarev;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

public abstract class Command {
    public static final int HEADER_LENGTH = 11;
    public static final int INTEGER_LENGTH = 4;
    public static final int LONG_LENGTH = 8;

    private final int id;

    protected Command(Supplier<Integer> idCounter) {
        id = idCounter.get();
    }

    public int getId() {
        return id;
    }

    protected void writeHeader(DataOutputStream stream, int length, byte commandSet, byte command) throws IOException {
        stream.writeInt(length);
        stream.writeInt(id);
        stream.writeByte(0);
        stream.writeByte(commandSet);
        stream.writeByte(command);
    }

    public static ReplyHeader readHeader(DataInputStream stream) throws IOException {
        int length = stream.readInt();
        int commandId = stream.readInt();
        byte flags = stream.readByte();
        short errorCode = stream.readShort();
        return new ReplyHeader(length - HEADER_LENGTH, commandId, flags, errorCode);
    }

    public abstract void writeCommand(DataOutputStream stream) throws IOException;

    public abstract void readReplyData(int dataLength, DataInputStream stream) throws IOException;
}
