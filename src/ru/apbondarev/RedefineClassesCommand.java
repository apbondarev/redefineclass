package ru.apbondarev;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class RedefineClassesCommand extends Command {
    private static final byte COMMAND_SET = 1;
    private static final byte COMMAND_ID = 18;

    private final SizesInfo sizesInfo;
    private final Map<Long, byte[]> classes;

    public RedefineClassesCommand(Supplier<Integer> idCounter, SizesInfo sizesInfo, Map<Long, byte[]> classes) {
        super(idCounter);
        this.sizesInfo = sizesInfo;
        this.classes = classes;
    }

    @Override
    public void writeCommand(DataOutputStream stream) throws IOException {
        int length = HEADER_LENGTH + INTEGER_LENGTH;
        for (Map.Entry<Long, byte[]> entry : classes.entrySet()) {
            length += sizesInfo.getReferenceTypeIDSize();
            length += INTEGER_LENGTH;
            length += entry.getValue().length;
        }
        writeHeader(stream, length, COMMAND_SET, COMMAND_ID);
        stream.writeInt(classes.size());
        for (Map.Entry<Long, byte[]> entry : classes.entrySet()) {
            long referenceTypeID = entry.getKey();
            byte[] bytes = entry.getValue();
            sizesInfo.writeReferenceTypeID(referenceTypeID, stream);
            stream.writeInt(bytes.length);
            stream.write(bytes);
        }
    }

    @Override
    public void readReplyData(int dataLength, DataInputStream stream) throws IOException {
        if (dataLength != 0) {
            throw new IllegalArgumentException("protocol error: " + dataLength);
        }
    }
}
