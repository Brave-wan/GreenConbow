/*
 * Copyright (C) 2013 Sebastian Kaspari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ijourney.ani.sample.discovery;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;


/**
 * Created by lvning on 2018/10/18.
 */
class DiscoveryThread extends Thread {
    private static final String TAG = "ANI/DiscoveryThread";
    private static final int MAXIMUM_PACKET_BYTES = 1024;

    private String multicastAddress;
    private int port;
    private MulticastSocket socket;
    private DiscoveryListener listener;

    private volatile boolean running;

    /* package-private */ DiscoveryThread(String multicastAddress, int port, DiscoveryListener listener) {
        this.multicastAddress = multicastAddress;
        this.port = port;
        this.listener = listener;
    }

    public void run() {
        running = true;

        listener.onDiscoveryStarted();

        try {
            socket = createSocket();
            receiveIntents();
        } catch (IOException exception) {
            if (running) {
                listener.onDiscoveryError(exception);
            }
        } finally {
            closeSocket();
        }

        listener.onDiscoveryStopped();
    }

    protected MulticastSocket createSocket() throws UnknownHostException, IOException {
        InetAddress address = InetAddress.getByName(multicastAddress);

        MulticastSocket socket = new MulticastSocket(port);
        socket.joinGroup(address);
        socket.setLoopbackMode(false);

        return socket;
    }

    private void closeSocket() {
        if (socket != null) {
            socket.close();
        }
    }

    public void stopDiscovery() {
        running = false;

        closeSocket();
    }

    protected void receiveIntents() throws IOException {
        while (running) {
            DatagramPacket packet = new DatagramPacket(
                    new byte[MAXIMUM_PACKET_BYTES], MAXIMUM_PACKET_BYTES
            );

            try {
                socket.receive(packet);

                byte[] data = packet.getData();
                int length = packet.getLength();

                String msg = new String(data, 0, length);

                listener.onIntentDiscovered(packet.getAddress(), msg);
            } catch (Exception exception) {
                Log.v(TAG, "Received UDP packet that could not be parsed as Intent");
            }
        }
    }
}