package com.uiza.sdk.utils;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;


/**
 * Utility class for converting objects to and from JSON using Jackson implementation.
 *
 * @author namnd
 * @see ObjectMapper
 */
public final class JacksonUtils {
    private JacksonUtils() {
        throw new IllegalArgumentException("Can not create instance it");
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * @param <T> the type of the desired object
     * @param src the Object for which JSON representation is to be created
     * @return Json representation of {@code list}
     */
    @NonNull
    public static <T> String toJson(@NonNull T src) {
        try {
            return mapper.writeValueAsString(src);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    /**
     * {@link ObjectMapper#readValue(String, Class)}
     *
     * @param <T>      the type of the desired object
     * @param json     the string from which the object is to be deserialized
     * @param classOfT the class of T
     * @return an object of type T from the string. Returns {@code null} if
     * {@code json} is {@code null}.
     */
    public static <T> T fromJson(@NonNull final String json, @NonNull final Class<T> classOfT) {
        try {
            return mapper.readValue(json, classOfT);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * {@link ObjectMapper#readValue(String, Class)}
     *
     * @param <T>      the type of the desired object
     * @param reader   the Reader from which the object is to be deserialized
     * @param classOfT the class of T
     * @return an object of type T from the string. Returns {@code null} if
     * {@code json} is {@code null}.
     */
    public static <T> T fromJson(@NonNull final Reader reader, @NonNull final Class<T> classOfT) {
        try {
            return mapper.readValue(reader, classOfT);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * {@link ObjectMapper#readValue(String, TypeReference)}
     *
     * @param <T>  the type of the desired object
     * @param json the string from which the object is to be deserialized
     * @return an List of type T from the string. Returns {@code Collections#emptyList} if
     * {@code json} is {@code null}.
     */
    public static <T> List<T> fromJson(@NonNull final String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<T>>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    /**
     * {@link ObjectMapper#readValue(String, TypeReference)}
     *
     * @param <T>    the type of the desired object
     * @param reader the Reader from which the object is to be deserialized
     * @return an List of type T from the string. Returns {@code Collections#emptyList} if
     * {@code json} is {@code null}.
     */
    public static <T> List<T> fromJson(@NonNull final Reader reader) {
        try {
            return mapper.readValue(reader, new TypeReference<List<T>>() {
            });
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
