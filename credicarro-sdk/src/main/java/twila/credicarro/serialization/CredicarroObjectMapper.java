package twila.credicarro.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * {@link ObjectMapper} central para (de)serializar requests/responses e o {@code ProblemDetails} de erro.
 * Datas são escritas em ISO-8601 com offset (nunca como timestamp numérico) e campos nulos são omitidos,
 * pois a API trata campos opcionais ausentes de forma diferente de {@code null}.
 */
public final class CredicarroObjectMapper {

    public static final ObjectMapper DEFAULT = create();

    private CredicarroObjectMapper() {
    }

    private static ObjectMapper create() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
        return mapper;
    }
}
