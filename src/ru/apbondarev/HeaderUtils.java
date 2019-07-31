package ru.apbondarev;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class HeaderUtils {

    private HeaderUtils() {}

    public static void write(DataOutputStream stream, int id, int length, byte commandSet, byte command) throws IOException {
        stream.writeInt(length);
        stream.writeInt(id);
        stream.writeByte(0);
        stream.writeByte(commandSet);
        stream.writeByte(command);
    }

    public static ReplyHeader read(DataInputStream stream) throws IOException {
        int length = stream.readInt();
        int id = stream.readInt();
        byte flags = stream.readByte();
        short errorCode = stream.readShort();
        return new ReplyHeader(length - Command.HEADER_LENGTH, id, flags, errorCode);
    }
}
