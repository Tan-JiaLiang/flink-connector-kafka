package org.apache.flink.connector.kafka.source.reader;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/** A wrapper class for ConsumerRecord. */
public class KafkaConsumerRecord {

    private final ConsumerRecord<byte[], byte[]> consumerRecord;
    private final long fetchTime;

    public KafkaConsumerRecord(ConsumerRecord<byte[], byte[]> consumerRecord, long fetchTime) {
        this.fetchTime = fetchTime;
        this.consumerRecord = consumerRecord;
    }

    public long getFetchTime() {
        return fetchTime;
    }

    public ConsumerRecord<byte[], byte[]> getConsumerRecord() {
        return consumerRecord;
    }
}
