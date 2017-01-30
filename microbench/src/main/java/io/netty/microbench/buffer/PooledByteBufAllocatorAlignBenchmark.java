/*
* Copyright 2017 The Netty Project
*
* The Netty Project licenses this file to you under the Apache License,
* version 2.0 (the "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at:
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations
* under the License.
*/
package io.netty.microbench.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.microbench.util.AbstractMicrobenchmark;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class PooledByteBufAllocatorAlignBenchmark extends AbstractMicrobenchmark {

    private static final Random rand = new Random();

    /**
     * Total chunk size we are walking.
     */
    @Param({ "00256", "01024", "04096", "16384", "65536" })
    private int size;

    @Param({ "0", "64" })
    private int cacheAlign;

    private PooledByteBufAllocator pooledAllocator;

    private ByteBuf pooledDirectBuffers;

    private int block;

    private byte[] bytes;

    @Setup(Level.Iteration)
    public void doSetup() {
        pooledAllocator = new PooledByteBufAllocator(true, 4, 4, 8192, 11, 0,
                0, 0, true, cacheAlign);
        pooledDirectBuffers = pooledAllocator.directBuffer(size);
        block = size / 256;
        bytes = new byte[block];
        rand.nextBytes(bytes);
    }

    @TearDown(Level.Iteration)
    public void doTearDown() {
        pooledDirectBuffers.release();
    }

    @Benchmark
    public void writeRead() {
        pooledDirectBuffers.writeBytes(bytes);
        pooledDirectBuffers.readBytes(block);
    }

    @Benchmark
    public void write() {
        pooledDirectBuffers.writeBytes(bytes);
    }
}
