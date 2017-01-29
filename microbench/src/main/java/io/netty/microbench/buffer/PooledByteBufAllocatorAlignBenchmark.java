package io.netty.microbench.buffer;

import java.util.HashMap;
import java.util.Map;
/*
* Copyright 2014 The Netty Project
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
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.microbench.util.AbstractMicrobenchmark;

@State(Scope.Thread)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class PooledByteBufAllocatorAlignBenchmark extends AbstractMicrobenchmark {

    private static final int MAX_LIVE_BUFFERS = 8192;

    private static final Random rand = new Random();

    /**
     * Total chunk size we are walking.
     */
    @Param({ "00000", "00256", "01024", "04096", "16384", "65536" })
    private int size;

    @Param({ "0", "64" })
    private int cacheAlign;

    private Map<Integer, ByteBufAllocator> pooledAllocatorMap  = new HashMap<Integer, ByteBufAllocator>();

    private Map<Integer, ByteBuf[]> pooledDirectBuffersMap = new HashMap<Integer, ByteBuf[]>();

    @Setup
    public void init() {
        PooledByteBufAllocator pooledAllocator = new PooledByteBufAllocator(true, 4, 4, 8192, 11, 0, 0, 0, true,
                cacheAlign);
        pooledAllocatorMap.put(size, pooledAllocator);
        ByteBuf[] pooledDirectBuffers = new ByteBuf[MAX_LIVE_BUFFERS];
        for (int i = 0; i < pooledDirectBuffers.length; i++) {
            pooledDirectBuffers[i] = pooledAllocator.directBuffer(size);
        }
        pooledDirectBuffersMap.put(size, pooledDirectBuffers);
    }

    @TearDown
    public void cleanup() {
        ByteBuf[] pooledDirectBuffers = pooledDirectBuffersMap.get(size);
        for (int i = 0; i < pooledDirectBuffers.length; i++) {
            pooledDirectBuffers[i].release();
        }
    }

    @Benchmark
    public void writeRead() {
        ByteBuf[] pooledDirectBuffers = pooledDirectBuffersMap.get(size);
        int block = size / 128;
        for (int i = 0; i < pooledDirectBuffers.length; i++) {
            byte[] bytes = new byte[block];
            rand.nextBytes(bytes);
            pooledDirectBuffers[i].writeBytes(bytes);
        }
        for (int i = 0; i < pooledDirectBuffers.length; i++) {
            pooledDirectBuffers[i].readBytes(block);
        }
    }

    @Benchmark
    public void write() {
        ByteBuf[] pooledDirectBuffers = pooledDirectBuffersMap.get(size);
        int block = size / 128;
        for (int i = 0; i < pooledDirectBuffers.length; i++) {
            byte[] bytes = new byte[block];
            rand.nextBytes(bytes);
            pooledDirectBuffers[i].writeBytes(bytes);
        }
    }

}
