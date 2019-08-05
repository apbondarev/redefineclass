package ru.apbondarev;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.sun.jdi.connect.TransportTimeoutException;

/**
 * https://docs.oracle.com/javase/8/docs/platform/jpda/jdwp/jdwp-protocol.html#JDWP_VirtualMachine_RedefineClasses
 */
public class RedefineClass implements Closeable {

    static final long DEFAULT_TIMEOUT = 5000L;

    private Socket socket;
    private boolean closed;
    private DataOutputStream socketOutput;
    private DataInputStream socketInput;
    private Supplier<Integer> idCounter = new Supplier<>() {
        private int id;
        @Override
        public Integer get() {
            id++;
            return id;
        }
    };

    public static void main(String[] args) {
        System.out.print("connecting to " + args[0] + " ...");
        try (RedefineClass main = new RedefineClass()) {
            main.attach(args[0], DEFAULT_TIMEOUT, DEFAULT_TIMEOUT);
            System.out.println(" connected");
        } catch (IOException e) {
            System.out.println();
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void attach(String address, long attachTimeout, long handshakeTimeout) throws IOException {
        if (address == null) {
            throw new NullPointerException("address is null");
        } else if (attachTimeout >= 0L && handshakeTimeout >= 0L) {
            int colonIndex = address.indexOf(':');
            String hostName;
            String portStr;
            if (colonIndex < 0) {
                hostName = InetAddress.getLocalHost().getHostName();
                portStr = address;
            } else {
                hostName = address.substring(0, colonIndex);
                portStr = address.substring(colonIndex + 1);
            }

            int port;
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("unable to parse port number in address");
            }

            InetSocketAddress socketAddress = new InetSocketAddress(hostName, port);
            Socket socket = new Socket();

            try {
                socket.connect(socketAddress, (int)attachTimeout);
            } catch (SocketTimeoutException e) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                throw new TransportTimeoutException("timed out trying to establish connection");
            }

            try {
                handshake(socket, handshakeTimeout);
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                throw e;
            }

            this.socket = socket;
        } else {
            throw new IllegalArgumentException("timeout is negative");
        }
    }

    private void handshake(Socket socket, long timeout) throws IOException {
        socket.setSoTimeout((int) timeout);
        byte[] bytesOut = "JDWP-Handshake".getBytes(StandardCharsets.UTF_8);
        socket.getOutputStream().write(bytesOut);
        byte[] bytesIn = new byte[bytesOut.length];

        int n;
        for (int i = 0; i < bytesOut.length; i += n) {
            try {
                n = socket.getInputStream().read(bytesIn, i, bytesOut.length - i);
            } catch (SocketTimeoutException e) {
                throw new IOException("handshake timeout");
            }

            if (n < 0) {
                socket.close();
                throw new IOException("handshake failed - connection prematurally closed");
            }
        }

        for (int i = 0; i < bytesOut.length; ++i) {
            if (bytesIn[i] != bytesOut[i]) {
                throw new IOException("handshake failed - unrecognized message from target VM");
            }
        }

        socket.setSoTimeout(0);
        socket.setTcpNoDelay(true);
        socketInput = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        socketOutput = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void redefine(String className, byte[] data) throws IOException {
        IDSizesCommand idSizesCommand = new IDSizesCommand(idCounter);
        executeCommands(Collections.singletonList(idSizesCommand));
        SizesInfo sizeInfo = idSizesCommand.getSizesInfo();

        List<ClassesBySignatureCommand> findClassCommands = new ArrayList<>();
        for (String it : Collections.singletonList(className)) {
            ClassesBySignatureCommand cmd = new ClassesBySignatureCommand(idCounter, it, sizeInfo);
            findClassCommands.add(cmd);
        }
        executeCommands(findClassCommands);

        Map<String, Long> classToReferenceId = new HashMap<>();
        for (ClassesBySignatureCommand cmd : findClassCommands) {
            long referenceTypeID = cmd.getResult().get(0).getTypeID();
            classToReferenceId.put(cmd.getClassName(), referenceTypeID);
            System.out.println("class: " + cmd.getClassName() + ", id: " + referenceTypeID);
        }
    }

    private void executeCommands(List<? extends Command> commands) throws IOException {
        Map<Integer, Command> itTocommands = new HashMap<>();
        for (Command cmd : commands) {
            cmd.writeCommand(socketOutput);
            itTocommands.put(cmd.getId(), cmd);
        }
        socketOutput.flush();
        while (!itTocommands.isEmpty()) {
            ReplyHeader reply = HeaderUtils.read(socketInput);
            if (itTocommands.containsKey(reply.getId())) {
                Command cmd = itTocommands.get(reply.getId());
                itTocommands.remove(reply.getId());
                cmd.readReplyData(reply.getDataLength(), socketInput);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            if (socketOutput != null) {
                socketOutput.close();
            }
            if (socketInput != null) {
                socketInput.close();
            }
            if (socket != null) {
                socket.close();
            }
            closed = true;
        }
    }
}
