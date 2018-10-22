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


import com.ijourney.ani.sample.internal.AndroidNetworkIntents;

/**
 * Created by lvning on 2018/10/18.
 */
public class Discovery {
    private String multicastAddress;
    private int port;

    private DiscoveryListener listener;
    private DiscoveryThread thread;

    public Discovery() {
        this(
                AndroidNetworkIntents.DEFAULT_MULTICAST_ADDRESS,
                AndroidNetworkIntents.DEFAULT_PORT
        );
    }

    public Discovery(int port) {
        this(
                AndroidNetworkIntents.DEFAULT_MULTICAST_ADDRESS,
                port
        );
    }

    public Discovery(String multicastAddress, int port) {
        this.multicastAddress = multicastAddress;
        this.port = port;
    }

    public void setDisoveryListener(DiscoveryListener listener) {
        this.listener = listener;
    }

    public void enable(DiscoveryListener listener) throws DiscoveryException {
        setDisoveryListener(listener);
        enable();
    }


    public void enable() throws DiscoveryException {
        if (listener == null) {
            throw new IllegalStateException("No listener set");
        }

        if (thread == null) {
            thread = createDiscoveryThread();
            thread.start();
        } else {
            throw new IllegalAccessError("Discovery already started");
        }
    }

    protected DiscoveryThread createDiscoveryThread() {
        return new DiscoveryThread(multicastAddress, port, listener);
    }

    public void disable() {
        if (thread != null) {
            thread.stopDiscovery();
            thread = null;
        } else {
            throw new IllegalAccessError("Discovery not running");
        }
    }
}
