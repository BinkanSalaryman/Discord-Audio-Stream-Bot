package net.runee.misc;

import java.nio.ByteBuffer;

public class MemoryQueue {
    public interface CopyDelegate<SRC, DST> {
        void copy(SRC source, int sourceOffset, DST destination, int destinationOffset, int length);
    }

    private int head;
    private int tail;
    private int length;
    private byte[] buffer;

    public int size() {
        return length;
    }

    public MemoryQueue() {
        this(2048);
    }

    public MemoryQueue(int capacity) {
        buffer = new byte[capacity];
    }

    public void clear() {
        head = 0;
        tail = 0;
        length = 0;
    }

    public byte[] buffer() {
        return buffer;
    }

    public int capacity() {
        return buffer.length;
    }

    private void ensureCapacity(int capacity) {
        if (capacity > buffer.length) {
            setCapacity((capacity + 2047) & ~2047);
        }
    }

    private void setCapacity(int capacity) {
        byte[] newBuffer = new byte[capacity];

        if (length > 0) {
            if (head < tail) {
                System.arraycopy(buffer, head, newBuffer, 0, length);
            } else {
                System.arraycopy(buffer, head, newBuffer, 0, buffer.length - head);
                System.arraycopy(buffer, 0, newBuffer, buffer.length - head, tail);
            }
        }

        head = 0;
        tail = length;
        buffer = newBuffer;
    }

    public <T> void enqueue(T source, int count, CopyDelegate<T, byte[]> copyFrom) {
        if (count == 0) {
            return;
        }

        ensureCapacity(this.length + count);

        if (head < tail) {
            int rightLength = buffer.length - tail;

            if (rightLength >= count) {
                copyFrom.copy(source, 0, buffer, tail, count);
            } else {
                copyFrom.copy(source, 0, buffer, tail, rightLength);
                copyFrom.copy(source, rightLength, buffer, 0, count - rightLength);
            }
        } else {
            copyFrom.copy(source, 0, buffer, tail, count);
        }
        tail = (tail + count) % buffer.length;
        this.length += count;
    }

    public void enqueue(byte[] buffer, int offset, int count) {
        enqueue(buffer, count, (src, sOff, dst, dOff, len) -> {
            System.arraycopy(src, sOff + offset, dst, dOff, len);
        });
    }

    public void enqueue(ByteBuffer buffer, int count) {
        if(buffer.hasArray()) {
            enqueue(buffer.array(), buffer.arrayOffset(), count);
        } else {
            enqueue(buffer, count, (src, sOff, dst, dOff, len) -> {
                for (int i = 0; i < len; i++) {
                    dst[dOff + i] = src.get(sOff + i);
                }
            });
        }
    }

    public <T> int dequeue(T destination, int count, CopyDelegate<byte[], T> copyTo) {
        if (count > this.length) {
            count = this.length;
        }

        if (count == 0) {
            return 0;
        }

        if (head < tail) {
            copyTo.copy(buffer, head, destination, 0, count);
        } else {
            int rightLength = (buffer.length - head);

            if (rightLength >= count) {
                copyTo.copy(buffer, head, destination, 0, count);
            } else {
                copyTo.copy(buffer, head, destination, 0, rightLength);
                copyTo.copy(buffer, 0, destination, rightLength, count - rightLength);
            }
        }

        head = (head + count) % buffer.length;
        this.length -= count;

        if (this.length == 0) {
            head = 0;
            tail = 0;
        }
        return count;
    }

    public int dequeue(byte[] buffer, int offset, int count) {
        return dequeue(buffer, count, (src, sOff, dst, dOff, len) -> {
            if(dst != null) {
                System.arraycopy(src, sOff, dst, dOff + offset, len);
            }
        });
    }

    public int dequeue(ByteBuffer buffer, int count) {
        if(buffer.hasArray()) {
            return dequeue(buffer.array(), buffer.arrayOffset(), count);
        } else {
            return dequeue(buffer, count, (src, sOff, dst, dOff, len) -> {
                for (int i = 0; i < len; i++) {
                    dst.put(dOff + i, src[sOff + i]);
                }
            });
        }
    }

    public int dequeue(int count) {
        return dequeue(null, count, (src, sOff, dst, dOff, len) -> {});
    }
}
