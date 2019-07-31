package ru.apbondarev;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ClassesBySignatureCommand implements Command {
    private static final byte COMMAND_SET = 1;
    private static final byte COMMAND_ID = 2;
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    private final int id;
    private final String className;
    private final String signature;

    public static class ClassInfo {
        private final byte refTypeTag;
        private final int typeID;
        private final int status;

        public ClassInfo(byte refTypeTag, int typeID, int status) {
            this.refTypeTag = refTypeTag;
            this.typeID = typeID;
            this.status = status;
        }

        public byte getRefTypeTag() {
            return refTypeTag;
        }

        public int getTypeID() {
            return typeID;
        }

        public int getStatus() {
            return status;
        }
    }

    private final List<ClassInfo> result = new ArrayList<>();

    public ClassesBySignatureCommand(int id, String classname) {
        this.id = id;
        className = classname;
        signature = 'L' + DOT_PATTERN.matcher(classname).replaceAll("/") + ';';
    }

    @Override
    public void writeCommand(DataOutputStream stream) throws IOException {
        byte[] bytes = signature.getBytes(StandardCharsets.UTF_8);
        int length = HEADER_LENGTH + INTEGER_LENGTH + bytes.length;
        HeaderUtils.write(stream, id, length, COMMAND_SET, COMMAND_ID);
        stream.writeInt(signature.length());
        stream.write(bytes);
    }

    @Override
    public void readReplyData(int dataLength, DataInputStream stream) throws IOException {
        int classes = stream.readInt();
        int byteCounter = 0;
        for (int i = 0; i < classes; i++) {
            byte refTypeTag = stream.readByte();
            byteCounter += 1;
            int typeID = stream.readInt();
            byteCounter += INTEGER_LENGTH;
            int status = stream.readInt();
            byteCounter += INTEGER_LENGTH;
            result.add(new ClassInfo(refTypeTag, typeID, status));
        }
        if (byteCounter > dataLength) {
            throw new IllegalStateException("protocol error");
        }
    }

    public String getClassName() {
        return className;
    }

    public List<ClassInfo> getResult() {
        return result;
    }
}
