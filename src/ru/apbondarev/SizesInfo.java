package ru.apbondarev;

public class SizesInfo {
    private final int fieldIDSize;
    private final int methodIDSize;
    private final int objectIDSize;
    private final int referenceTypeIDSize;
    private final int frameIDSize;

    public SizesInfo(int fieldIDSize, int methodIDSize, int objectIDSize, int referenceTypeIDSize, int frameIDSize) {
        this.fieldIDSize = fieldIDSize;
        this.methodIDSize = methodIDSize;
        this.objectIDSize = objectIDSize;
        this.referenceTypeIDSize = referenceTypeIDSize;
        this.frameIDSize = frameIDSize;
    }

    public int getFieldIDSize() {
        return fieldIDSize;
    }

    public int getMethodIDSize() {
        return methodIDSize;
    }

    public int getObjectIDSize() {
        return objectIDSize;
    }

    public int getReferenceTypeIDSize() {
        return referenceTypeIDSize;
    }

    public int getFrameIDSize() {
        return frameIDSize;
    }
}
