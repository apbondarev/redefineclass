Compile and redefine java class via debugger protocol.

https://docs.oracle.com/javase/8/docs/platform/jpda/jdwp/jdwp-protocol.html#JDWP_VirtualMachine_RedefineClasses

Usage:

	java -jar redefine.jar hostname:port javafile [compiler arguments]

Example:

	java -jar redefine.jar 127.0.0.1:50010 SampleApp.java -source 12 -target 12 -classpath ...