package ru.apbondarev;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ClassesBySignatureCommand extends Command {
    private static final byte COMMAND_SET = 1;
    private static final byte COMMAND_ID = 2;
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    private final String className;
    private final String signature;
    private final SizesInfo sizesInfo;

    public static class ClassInfo {
        private final byte refTypeTag;
        private final long typeID;
        private final int status;

        public ClassInfo(byte refTypeTag, long typeID, int status) {
            this.refTypeTag = refTypeTag;
            this.typeID = typeID;
            this.status = status;
        }

        public byte getRefTypeTag() {
            return refTypeTag;
        }

        public long getTypeID() {
            return typeID;
        }

        public int getStatus() {
            return status;
        }
    }

    private final List<ClassInfo> result = new ArrayList<>();

    public ClassesBySignatureCommand(Supplier<Integer> idCounter, String classname, SizesInfo sizesInfo) {
        super(idCounter);
        className = classname;
        signature = 'L' + DOT_PATTERN.matcher(classname).replaceAll("/") + ';';
        this.sizesInfo = sizesInfo;
    }

    @Override
    public void writeCommand(DataOutputStream stream) throws IOException {
        byte[] bytes = signature.getBytes(StandardCharsets.UTF_8);
        int length = HEADER_LENGTH + INTEGER_LENGTH + bytes.length;
        HeaderUtils.write(stream, getId(), length, COMMAND_SET, COMMAND_ID);
        stream.writeInt(signature.length());
        stream.write(bytes);
    }

    @Override
    public void readReplyData(int dataLength, DataInputStream stream) throws IOException {
        int classes = stream.readInt();
        int byteCounter = INTEGER_LENGTH;
        for (int i = 0; i < classes; i++) {
            byte refTypeTag = stream.readByte();
            byteCounter += 1;
            long typeID;
            if (sizesInfo.getReferenceTypeIDSize() == INTEGER_LENGTH) {
                typeID = stream.readInt();
                byteCounter += INTEGER_LENGTH;
            } else if (sizesInfo.getReferenceTypeIDSize() == LONG_LENGTH) {
                typeID = stream.readLong();
                byteCounter += LONG_LENGTH;
            } else {
                throw new IllegalStateException("unexpected ReferenceTypeIDSize=" + sizesInfo.getReferenceTypeIDSize());
            }
            int status = stream.readInt();
            byteCounter += INTEGER_LENGTH;
            result.add(new ClassInfo(refTypeTag, typeID, status));
        }
        if (byteCounter != dataLength) {
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
