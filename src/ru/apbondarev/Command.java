package ru.apbondarev;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Command {
    int HEADER_LENGTH = 11;
    int INTEGER_LENGTH = 4;

    void writeCommand(DataOutputStream stream) throws IOException;

    void readReplyData(int dataLength, DataInputStream stream) throws IOException;
}
