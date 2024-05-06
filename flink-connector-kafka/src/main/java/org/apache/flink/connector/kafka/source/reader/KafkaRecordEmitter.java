/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.connector.kafka.source.reader;

import org.apache.flink.annotation.Internal;
import org.apache.flink.api.connector.source.SourceOutput;
import org.apache.flink.connector.base.source.reader.RecordEmitter;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.connector.kafka.source.split.KafkaPartitionSplitState;
import org.apache.flink.util.Collector;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;

/** The {@link RecordEmitter} implementation for {@link KafkaSourceReader}. */
@Internal
public class KafkaRecordEmitter<T>
        implements RecordEmitter<KafkaConsumerRecord, T, KafkaPartitionSplitState> {

    private final KafkaRecordDeserializationSchema<T> deserializationSchema;
    private final SourceOutputWrapper<T> sourceOutputWrapper = new SourceOutputWrapper<>();

    public KafkaRecordEmitter(KafkaRecordDeserializationSchema<T> deserializationSchema) {
        this.deserializationSchema = deserializationSchema;
    }

    @Override
    public void emitRecord(
            KafkaConsumerRecord record, SourceOutput<T> output, KafkaPartitionSplitState splitState)
            throws Exception {
        try {
            ConsumerRecord<byte[], byte[]> consumerRecord = record.getConsumerRecord();
            long fetchTime = record.getFetchTime();

            sourceOutputWrapper.setSourceOutput(output);
            sourceOutputWrapper.setTimestamp(consumerRecord.timestamp());
            sourceOutputWrapper.setFetchTime(fetchTime);
            deserializationSchema.deserialize(consumerRecord, sourceOutputWrapper);
            splitState.setCurrentOffset(consumerRecord.offset() + 1);
        } catch (Exception e) {
            throw new IOException("Failed to deserialize consumer record due to", e);
        }
    }

    private static class SourceOutputWrapper<T> implements Collector<T> {

        private SourceOutput<T> sourceOutput;
        private long timestamp;
        private long fetchTime;

        @Override
        public void collect(T record) {
            // todo: use sourceOutput.collect(record, timestamp, fetchTime);
            sourceOutput.collect(record, timestamp);
        }

        @Override
        public void close() {}

        private void setSourceOutput(SourceOutput<T> sourceOutput) {
            this.sourceOutput = sourceOutput;
        }

        private void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        private void setFetchTime(long fetchTime) {
            this.fetchTime = fetchTime;
        }
    }
}
