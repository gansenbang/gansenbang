/*
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 	        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License. See accompanying LICENSE file. 
 */
package io.s4.client;

import io.s4.collector.EventWrapper;
import io.s4.listener.EventHandler;
import io.s4.message.Request;
import io.s4.message.Response;
import io.s4.util.ByteArrayIOChannel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public abstract class ClientStub implements OutputStub, InputStub {

    protected static final Logger logger = Logger.getLogger("adapter");

    /**
     * Description of the protocol implemented by a concrete instance of this
     * stub.
     */
    public static class Info {
        public final String name;
        public final int versionMajor;
        public final int versionMinor;

        public Info(String name, int versionMajor, int versionMinor) {
            this.name = name;
            this.versionMajor = versionMajor;
            this.versionMinor = versionMinor;
        }
    }

    /**
     * Meta-information about the protocol that this stub uses to talk to
     * external clients.
     * 
     * This is sent to the client as a part of the handshake.
     */
    abstract public Info getProtocolInfo();

    /**
     * Stream names that are accepted by this stub to be forwarded to its
     * clients.
     */
    @Override
    public List<String> getAcceptedStreams() {
        return null;
    }

    private List<EventHandler> handlers = new ArrayList<EventHandler>();

    /**
     * A handler that can inject events produced by this stub into the S4
     * cluster.
     */
    @Override
    public void addHandler(EventHandler handler) {
        this.handlers.add(handler);
    }

    /**
     * Remove a handler.
     */
    @Override
    public boolean removeHandler(EventHandler handler) {
        return handlers.remove(handler);
    }

    /**
     * Convert an array of bytes into an event wrapper. This method is used to
     * translate data received from a client into events that may be injected
     * into the S4 cluster.
     * 
     * @param v
     *            array of bytes
     * @return EventWrapper constructed from the byte array.
     */
    abstract public EventWrapper eventWrapperFromBytes(byte[] v);

    /**
     * Convert an event wrapper into a byte array. Events received from the S4
     * cluster for dispatching to a client are translated into a byte array
     * using this method.
     * 
     * @param e
     *            an {@link EventWrapper}
     * @return a byte array
     */
    abstract public byte[] bytesFromEventWrapper(EventWrapper e);

    /**
     * Construct an I/O channel over which the stub can communicate with a
     * client. The channel allows arrys of bytes to be exchanged between the
     * stub and client.
     * 
     * @param socket
     *            TCP/IP socket
     * @return an IO Channel to send and recv byte arrays
     * @throws IOException
     *             if the underlying socket could not provide valid input and
     *             output streams.
     */
    public IOChannel createIOChannel(Socket socket) throws IOException {
        return new ByteArrayIOChannel(socket);
    }

    // send an event into the cluster via adapter.
    void injectEvent(EventWrapper e) {
        for (EventHandler handler : handlers) {
            handler.processEvent(e);
        }
    }

    // private List<ClientConnection> clients = new
    // ArrayList<ClientConnection>();
    HashMap<UUID, ClientConnection> clients = new HashMap<UUID, ClientConnection>();

    /**
     * Create a client connection and add it to list of clients.
     * 
     * @param socket
     *            client's I/O socket
     */
    private void addClient(ClientConnection c) {
        synchronized (clients) {
            logger.info("adding client " + c.uuid);
            clients.put(c.uuid, c);
        }
    }

    LinkedBlockingQueue<EventWrapper> queue = new LinkedBlockingQueue<EventWrapper>();

    @Override
    public int getQueueSize() {
        return queue.size();
    }

    @Override
    public void queueWork(EventWrapper e) {
        queue.offer(e);
    }

    ServerSocket serverSocket = null;

    public void setConnectionPort(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    private Thread acceptThread = null;
    private Thread senderThread = null;

    public void init() {
        // start accepting new clients and sending events to them
        (acceptThread = new Thread(connectionListener)).start();
        (senderThread = new Thread(sender)).start();
    }

    public void shutdown() {
        // stop accepting new clients
        if (acceptThread != null) {
            acceptThread.interrupt();
            acceptThread = null;
        }

        // stop sending events to them.
        if (senderThread != null) {
            senderThread.interrupt();
            senderThread = null;
        }

        // stop all connected clients.
        List<ClientConnection> clientCopy = new ArrayList<ClientConnection>(clients.values());
        for (ClientConnection c : clientCopy) {
            c.stop();
            c.close();
        }
    }

    private final Runnable connectionListener = new Runnable() {

        Handshake handshake = null;

        public void run() {
            if (handshake == null)
                handshake = new Handshake(ClientStub.this);

            try {
                while (serverSocket != null && serverSocket.isBound()
                        && !Thread.currentThread().isInterrupted()) {

                    Socket socket = serverSocket.accept();

                    ClientConnection connection = handshake.execute(socket);

                    if (connection != null) {
                        addClient(connection);
                        connection.start();
                    }

                }
            } catch (IOException e) {
                logger.info("exception in client connection listener", e);
            }
        }

    };

    public final Runnable sender = new Runnable() {
        ArrayList<ClientConnection> disconnect = new ArrayList<ClientConnection>();

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    EventWrapper event = queue.take();

                    // Responses need special handling.
                    if (event.getEvent() instanceof Response) {
                        dispatchResponse(event);
                        continue;
                    }

                    // TODO: include check to see if the event belongs to a
                    // particular client.

                    dispatchToAllClients(event);

                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        private void dispatchToAllClients(EventWrapper event) {

            byte[] b = bytesFromEventWrapper(event);
            String stream = event.getStreamName();
            
            synchronized (clients) {
                for (ClientConnection c : clients.values()) {
                    if (c.good() && c.streamAccepted(stream)) {
                        try {
                            c.io.send(b);

                        } catch (IOException e) {
                            logger.error("error sending message to client "
                                    + c.uuid + ". disconnecting", e);

                            disconnect.add(c);
                        }
                    }
                }
            }

            if (disconnect.size() > 0) {
                for (ClientConnection d : disconnect)
                    d.close();

                disconnect.clear();
            }
        }

        private void dispatchResponse(EventWrapper event) {
            Response res = (Response) event.getEvent();
            Request.RInfo rinfo = res.getRInfo();

            if (rinfo instanceof Request.ClientRInfo) {
                UUID uuid = ((Request.ClientRInfo) rinfo).getRequesterUUID();

                ClientConnection c = clients.get(uuid);

                if (c != null && c.good() && c.clientReadMode.takePrivate()) {
                    try {
                        byte[] b = bytesFromEventWrapper(event);
                        c.io.send(b);

                    } catch (IOException e) {
                        logger.error("error sending response to client "
                                + c.uuid + ". disconnecting", e);

                        c.close();
                    }

                } else {
                    logger.warn("no active client found for response: " + res);
                }
            }
        }
    };
}
