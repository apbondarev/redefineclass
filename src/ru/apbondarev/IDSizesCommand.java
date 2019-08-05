package ru.apbondarev;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

public class IDSizesCommand extends Command {
    private static final byte COMMAND_SET = 1;
    private static final byte COMMAND_ID = 7;

    private SizesInfo sizesInfo;

    public IDSizesCommand(Supplier<Integer> idCounter) {
        super(idCounter);
    }

    @Override
    public void writeCommand(DataOutputStream stream) throws IOException {
        HeaderUtils.write(stream, getId(), HEADER_LENGTH, COMMAND_SET, COMMAND_ID);
    }

    @Override
    public void readReplyData(int dataLength, DataInputStream stream) throws IOException {
        if (dataLength != 5 * INTEGER_LENGTH) {
            throw new IllegalArgumentException(String.valueOf(dataLength));
        }
        int fieldIDSize = stream.readInt();
        int methodIDSize = stream.readInt();
        int objectIDSize = stream.readInt();
        int referenceTypeIDSize = stream.readInt();
        int frameIDSize = stream.readInt();
        sizesInfo = new SizesInfo(fieldIDSize, methodIDSize, objectIDSize, referenceTypeIDSize, frameIDSize);
    }

    public SizesInfo getSizesInfo() {
        return sizesInfo;
    }
}
