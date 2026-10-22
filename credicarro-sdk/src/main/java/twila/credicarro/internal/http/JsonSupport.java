package twila.credicarro.internal.http;

import com.fasterxml.jackson.databind.JavaType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.internal.generated.PagedResultWire;
import twila.credicarro.serialization.CredicarroObjectMapper;

/**
 * Serialização do corpo das requisições e leitura das respostas (incluindo o envelope paginado).
 */
public final class JsonSupport {

    private JsonSupport() {
    }

    public static byte[] write(Object value) {
        try {
            return CredicarroObjectMapper.DEFAULT.writeValueAsBytes(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao serializar o corpo da requisição.", ex);
        }
    }

    public static <T> T read(ApiResponse response, Class<T> type) {
        try {
            T value = CredicarroObjectMapper.DEFAULT.readValue(response.body(), type);
            if (value == null)
                throw new IllegalStateException("A API do Credicarro retornou uma resposta vazia.");

            return value;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao desserializar a resposta da API.", ex);
        }
    }

    public static <W, P> PagedResult<P> readPaged(ApiResponse response, Class<W> itemType, Function<W, P> mapper) {
        try {
            JavaType type = CredicarroObjectMapper.DEFAULT.getTypeFactory().constructParametricType(PagedResultWire.class, itemType);
            PagedResultWire<W> wire = CredicarroObjectMapper.DEFAULT.readValue(response.body(), type);

            List<P> items = new ArrayList<>();
            if (wire.items != null) {
                for (W item : wire.items)
                    items.add(mapper.apply(item));
            }

            if (wire.page == null)
                return new PagedResult<>(items, false, false, 1, items.size(), items.size());

            return new PagedResult<>(items, wire.page.hasNext, wire.page.hasPrevious, wire.page.number, wire.page.size, wire.page.total);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao desserializar a resposta da API.", ex);
        }
    }

    public static String pagedPath(String path, ListRequest request) {
        ListRequest effective = request != null ? request : ListRequest.defaults();

        return new QueryStringBuilder()
                .add("pagina_inicial", effective.getPage())
                .add("limite", effective.getPageSize())
                .build(path);
    }
}
