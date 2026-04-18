package com.restaurant.luch.supabase;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String str = json.getAsString();
        try {
            // Пробуем как OffsetDateTime (со смещением, например +00:00 или Z)
            OffsetDateTime odt = OffsetDateTime.parse(str, OFFSET_FORMATTER);
            return odt.toLocalDateTime();
        } catch (DateTimeParseException e1) {
            try {
                // Пробуем как LocalDateTime без смещения (ISO_LOCAL_DATE_TIME)
                return LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e2) {
                // Удаляем смещение вручную (Z или +hh:mm) и парсим оставшееся
                String withoutOffset = str.replaceAll("([+-]\\d{2}:\\d{2}|Z)$", "");
                return LocalDateTime.parse(withoutOffset, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        }
    }
}