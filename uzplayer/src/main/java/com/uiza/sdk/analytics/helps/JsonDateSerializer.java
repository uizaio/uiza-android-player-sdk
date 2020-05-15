package com.uiza.sdk.analytics.helps;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Date;

public class JsonDateSerializer extends JsonSerializer<Date> {

    private static final String DATE_TIME_FMT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null) {
            gen.writeString((new DateTime(value.getTime())).toString(DATE_TIME_FMT));
        }
    }
}
