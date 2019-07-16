package ru.apbondarev;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import com.sun.jdi.connect.TransportTimeoutException;

public class Main {

    private static final long DEFAULT_TIMEOUT = 5000L;

    private Socket socket;
    private boolean closed;
    private OutputStream socketOutput;
    private InputStream socketInput;

    public static void main(String[] args) {
        System.out.print("connecting to " + args[0] + " ...");
        Main main = new Main();
        try {
            main.attach(args[0], DEFAULT_TIMEOUT, DEFAULT_TIMEOUT);
        } catch (IOException e) {
            System.out.println();
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println(" connected");

        try {
            main.close();
        } catch (IOException ignore) {
        }
    }

    private void attach(String address, long attachTimeout, long handshakeTimeout) throws IOException {
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
        socketInput = socket.getInputStream();
        socketOutput = socket.getOutputStream();
    }

    private void close() throws IOException {
        if (!closed) {
            socketOutput.close();
            socketInput.close();
            socket.close();
            closed = true;
        }
    }
}
