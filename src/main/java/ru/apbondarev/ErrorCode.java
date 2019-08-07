package ru.apbondarev;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ErrorCode {
    NONE((short) 0, "No error has occurred"),
    INVALID_THREAD((short) 10, "Passed thread is null, is not a valid thread or has exited"),
    INVALID_THREAD_GROUP((short) 11, "Thread group invalid"),
    INVALID_PRIORITY((short) 12, "Invalid priority"),
    THREAD_NOT_SUSPENDED((short) 13, "If the specified thread has not been suspended by an event"),
    THREAD_SUSPENDED((short) 14, "Thread already suspended"),
    THREAD_NOT_ALIVE((short) 15, "Thread has not been started or is now dead"),
    INVALID_OBJECT((short) 20, "If this reference type has been unloaded and garbage collected"),
    INVALID_CLASS((short) 21, "Invalid class"),
    CLASS_NOT_PREPARED((short) 22, "Class has been loaded but not yet prepared"),
    INVALID_METHODID((short) 23, "Invalid method"),
    INVALID_LOCATION((short) 24, "Invalid location"),
    INVALID_FIELDID((short) 25, "Invalid field"),
    INVALID_FRAMEID((short) 30, "Invalid jframeID"),
    NO_MORE_FRAMES((short) 31, "There are no more Java or JNI frames on the call stack"),
    OPAQUE_FRAME((short) 32, "Information about the frame is not available"),
    NOT_CURRENT_FRAME((short) 33, "Operation can only be performed on current frame"),
    TYPE_MISMATCH((short) 34, "The variable is not an appropriate type for the function used"),
    INVALID_SLOT((short) 35, "Invalid slot"),
    DUPLICATE((short) 40, "Item already set"),
    NOT_FOUND((short) 41, "Desired element not found"),
    INVALID_MODULE((short) 42, "Invalid module"),
    INVALID_MONITOR((short) 50, "Invalid monitor"),
    NOT_MONITOR_OWNER((short) 51, "This thread doesn't own the monitor"),
    INTERRUPT((short) 52, "The call has been interrupted before completion"),
    INVALID_CLASS_FORMAT((short) 60, "The virtual machine attempted to read a class file and determined that the file is malformed or otherwise cannot be interpreted as a class file"),
    CIRCULAR_CLASS_DEFINITION((short) 61, "A circularity has been detected while initializing a class"),
    FAILS_VERIFICATION((short) 62, "The verifier detected that a class file, though well formed, contained some sort of internal inconsistency or security problem"),
    ADD_METHOD_NOT_IMPLEMENTED((short) 63, "Adding methods has not been implemented"),
    SCHEMA_CHANGE_NOT_IMPLEMENTED((short) 64, "Schema change has not been implemented"),
    INVALID_TYPESTATE((short) 65, "The state of the thread has been modified, and is now inconsistent"),
    HIERARCHY_CHANGE_NOT_IMPLEMENTED((short) 66, "A direct superclass is different for the new class version, or the set of directly implemented interfaces is different and canUnrestrictedlyRedefineClasses is false"),
    DELETE_METHOD_NOT_IMPLEMENTED((short) 67, "The new class version does not declare a method declared in the old class version and canUnrestrictedlyRedefineClasses is false"),
    UNSUPPORTED_VERSION((short) 68, "A class file has a version number not supported by this VM"),
    NAMES_DONT_MATCH((short) 69, "The class name defined in the new class file is different from the name in the old class object"),
    CLASS_MODIFIERS_CHANGE_NOT_IMPLEMENTED((short) 70, "The new class version has different modifiers and canUnrestrictedlyRedefineClasses is false"),
    METHOD_MODIFIERS_CHANGE_NOT_IMPLEMENTED((short) 71, "A method in the new class version has different modifiers than its counterpart in the old class version and canUnrestrictedlyRedefineClasses is false"),
    CLASS_ATTRIBUTE_CHANGE_NOT_IMPLEMENTED((short) 72, "The new class version has different NestHost or NestMembers class attribute and canUnrestrictedlyRedefineClasses is false"),
    NOT_IMPLEMENTED((short) 99, "The functionality is not implemented in this virtual machine"),
    NULL_POINTER((short) 100, "Invalid pointer"),
    ABSENT_INFORMATION((short) 101, "Desired information is not available"),
    INVALID_EVENT_TYPE((short) 102, "The specified event type id is not recognized"),
    ILLEGAL_ARGUMENT((short) 103, "Illegal argument"),
    OUT_OF_MEMORY((short) 110, "The function needed to allocate memory and no more memory was available for allocation"),
    ACCESS_DENIED((short) 111, "Debugging has not been enabled in this virtual machine. JVMTI cannot be used"),
    VM_DEAD((short) 112, "The virtual machine is not running"),
    INTERNAL((short) 113, "An unexpected internal error has occurred"),
    UNATTACHED_THREAD((short) 115, "The thread being used to call this function is not attached to the virtual machine. Calls must be made from attached threads"),
    INVALID_TAG((short) 500, "object type id or class tag"),
    ALREADY_INVOKING((short) 502, "Previous invoke not complete"),
    INVALID_INDEX((short) 503, "Index is invalid"),
    INVALID_LENGTH((short) 504, "The length is invalid"),
    INVALID_STRING((short) 506, "The string is invalid"),
    INVALID_CLASS_LOADER((short) 507, "The class loader is invalid"),
    INVALID_ARRAY((short) 508, "The array is invalid"),
    TRANSPORT_LOAD((short) 509, "Unable to load the transport"),
    TRANSPORT_INIT((short) 510, "Unable to initialize the transport"),
    NATIVE_METHOD((short) 511, ""),
    INVALID_COUNT((short) 512, "The count is invalid"),

    UNKNOWN((short) -1, "Unknown");

    private final short code;
    private final String description;

    private static final Map<Short, ErrorCode> map = Stream.of(values())
            .collect(Collectors.toMap(
                    ErrorCode::getCode,
                    it -> it
            ));

    ErrorCode(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ErrorCode valueOf(short value) {
        return map.getOrDefault(value, UNKNOWN);
    }

    public short getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ErrorCode{" + code + ", '" + description + "'}";
    }
}
