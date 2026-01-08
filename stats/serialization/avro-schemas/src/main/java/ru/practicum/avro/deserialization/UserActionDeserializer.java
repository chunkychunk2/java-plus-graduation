package ru.practicum.avro.deserialization;

public class UserActionDeserializer extends BaseAvroDeserializer<ru.practicum.ewm.stats.avro.UserActionAvro> {
    public UserActionDeserializer() {
        super(ru.practicum.ewm.stats.avro.UserActionAvro.getClassSchema());
    }
}
