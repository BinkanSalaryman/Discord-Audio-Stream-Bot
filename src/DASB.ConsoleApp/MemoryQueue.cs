using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace DASB {
    public class MemoryQueue {
        private static readonly CopyDelegate<IntPtr, byte[]> copyFromPointer = (src, sOff, dst, dOff, len) => Marshal.Copy(src + sOff, dst, dOff, len);
        private static readonly CopyDelegate<byte[], IntPtr> copyToPointer = (src, sOff, dst, dOff, len) => Marshal.Copy(src, sOff, dst + dOff, len);
        private static readonly CopyDelegate<Stream, byte[]> copyFromStream = (src, sOff, dst, dOff, len) => src.Read(dst, dOff, len);
        private static readonly CopyDelegate<byte[], Stream> copyToStream = (src, sOff, dst, dOff, len) => dst.Write(src, sOff, len);

        private int head;
        private int tail;
        private int length;
        private byte[] buffer;

        public int Length {
            get { return length; }
        }

        public MemoryQueue() : this(2048) {

        }

        public MemoryQueue(int capacity) {
            buffer = new byte[capacity];
        }

        public void Clear() {
            head = 0;
            tail = 0;
            length = 0;
        }

        private void EnsureCapacity(int capacity) {
            if (capacity > buffer.Length) {
                SetCapacity((capacity + 2047) & ~2047);
            }
        }

        private void SetCapacity(int capacity) {
            byte[] newBuffer = new byte[capacity];

            if (length > 0) {
                if (head < tail) {
                    Buffer.BlockCopy(buffer, head, newBuffer, 0, length);
                } else {
                    Buffer.BlockCopy(buffer, head, newBuffer, 0, buffer.Length - head);
                    Buffer.BlockCopy(buffer, 0, newBuffer, buffer.Length - head, tail);
                }
            }

            head = 0;
            tail = length;
            buffer = newBuffer;
        }

        public void Enqueue<T>(T source, int count, CopyDelegate<T, byte[]> copyFrom) {
            if (count == 0) {
                return;
            }

            lock (this) {
                EnsureCapacity(this.length + count);

                if (head < tail) {
                    int rightLength = buffer.Length - tail;

                    if (rightLength >= count) {
                        copyFrom(source, 0, buffer, tail, count);
                    } else {
                        copyFrom(source, 0, buffer, tail, rightLength);
                        copyFrom(source, rightLength, buffer, 0, count - rightLength);
                    }
                } else {
                    copyFrom(source, 0, buffer, tail, count);
                }
                tail = (tail + count) % buffer.Length;
                this.length += count;
            }
        }

        public void Enqueue(byte[] buffer, int offset, int count) {
            var pin = GCHandle.Alloc(buffer, GCHandleType.Pinned);

            Enqueue(pin.AddrOfPinnedObject() + offset, count, copyFromPointer);

            pin.Free();
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void Enqueue(Stream stream, int count) {
            Enqueue(stream, count, copyFromStream);
        }

        public int Dequeue<T>(T destination, int count, CopyDelegate<byte[], T> copyTo) {
            lock (this) {
                if (count > this.length) {
                    count = this.length;
                }

                if (count == 0) {
                    return 0;
                }

                if (head < tail) {
                    copyTo(buffer, head, destination, 0, count);
                } else {
                    int rightLength = (buffer.Length - head);

                    if (rightLength >= count) {
                        copyTo(buffer, head, destination, 0, count);
                    } else {
                        copyTo(buffer, head, destination, 0, rightLength);
                        copyTo(buffer, 0, destination, rightLength, count - rightLength);
                    }
                }

                head = (head + count) % buffer.Length;
                this.length -= count;

                if (this.length == 0) {
                    head = 0;
                    tail = 0;
                }
                return count;
            }
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public int Dequeue(Stream stream, int count) {
            return Dequeue(stream, count, copyToStream);
        }

        public int Dequeue(byte[] buffer, int offset, int count) {
            var pin = GCHandle.Alloc(buffer, GCHandleType.Pinned);

            int result = Dequeue(pin.AddrOfPinnedObject() + offset, count, copyToPointer);

            pin.Free();

            return result;
        }

        public Stream AsStream(FileAccess access) {
            return new MemoryQueueStream(this, access);
        }

        private class MemoryQueueStream : Stream {
            public readonly MemoryQueue Queue;
            public readonly FileAccess Access;
            public override bool CanRead => (Access & FileAccess.Read) != 0;
            public override bool CanSeek => false;
            public override bool CanWrite => (Access & FileAccess.Write) != 0;
            public override long Length => Queue.Length;
            public override long Position { get => throw new NotSupportedException(); set => throw new NotSupportedException(); }

            public MemoryQueueStream(MemoryQueue queue, FileAccess access) {
                this.Queue = queue;
                this.Access = access;
            }

            public override void Flush() {
                throw new NotSupportedException();
            }

            public override int Read(byte[] buffer, int offset, int count) {
                if (CanRead) {
                    return Queue.Dequeue(buffer, offset, count);
                } else {
                    throw new NotSupportedException();
                }
            }

            public override long Seek(long offset, SeekOrigin origin) {
                throw new NotSupportedException();
            }

            public override void SetLength(long value) {
                throw new NotSupportedException();
            }

            public override void Write(byte[] buffer, int offset, int count) {
                if (CanWrite) {
                    Queue.Enqueue(buffer, offset, count);
                } else {
                    throw new NotSupportedException();
                }
            }
        }
    }
}
