package com.github.steveice10.packetlib.packet;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;

import java.io.IOException;

/**
 * A network packet. Any given packet must have a constructor that takes in a {@link NetInput}.
 */
public interface Packet {
    /**
     * Writes the packet to the given output buffer.
     *
     * @param out The output destination to write to.
     * @throws IOException If an I/O error occurs.
     */
    void write(NetOutput out) throws IOException;

    /**
     * Gets whether the packet has handling priority.
     * If the result is true, the packet will be handled immediately after being
     * decoded.
     *
     * @return Whether the packet has priority.
     */
    default boolean isPriority() {
        return false;
    }
}
