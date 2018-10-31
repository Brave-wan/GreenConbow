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

package com.ijourney.ani.sample.transmitter;

import com.ijourney.ani.sample.internal.AndroidNetworkIntents;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


/**
 * Created by lvning on 2018/10/18.
 */
public class Transmitter {

    private String multicastAddress;
    private int port;

    public Transmitter() {
        this(
                AndroidNetworkIntents.DEFAULT_MULTICAST_ADDRESS,
                AndroidNetworkIntents.DEFAULT_PORT
        );
    }

    public Transmitter(int port) {
        this(
                AndroidNetworkIntents.DEFAULT_MULTICAST_ADDRESS,
                port
        );
    }

    public Transmitter(String multicastAddress, int port) {
        this.multicastAddress = multicastAddress;
        this.port = port;
    }

    public void transmit(String msg) throws TransmitterException {
        DatagramSocket socket = null;
        try {
            socket = createSocket();
            transmit(socket, msg);
        } catch (UnknownHostException exception) {
            throw new TransmitterException("Unknown host", exception);
        } catch (SocketException exception) {
            throw new TransmitterException("Can't create DatagramSocket", exception);
        } catch (IOException exception) {
            throw new TransmitterException("IOException during sending intent", exception);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    protected DatagramSocket  createSocket() throws IOException {
        return new DatagramSocket();
    }

    private void transmit(DatagramSocket socket, String msg) throws IOException {
        byte[] data = msg.getBytes("utf-8");

        DatagramPacket packet = new DatagramPacket(
                data,
                data.length,
                InetAddress.getByName(multicastAddress),
                port
        );

        socket.send(packet);
    }
}
