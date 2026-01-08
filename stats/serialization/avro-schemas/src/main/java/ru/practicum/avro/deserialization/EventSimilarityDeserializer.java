package ru.practicum.avro.deserialization;

public class EventSimilarityDeserializer extends BaseAvroDeserializer<ru.practicum.ewm.stats.avro.EventSimilarityAvro> {
    public EventSimilarityDeserializer() {
        super(ru.practicum.ewm.stats.avro.EventSimilarityAvro.getClassSchema());
    }
}
