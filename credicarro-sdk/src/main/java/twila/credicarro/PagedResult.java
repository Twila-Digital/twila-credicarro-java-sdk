package twila.credicarro;

import java.util.List;
import lombok.Value;

/**
 * Página de resultados (`itens` + `pagina`). Não há auto-paginação: use {@link #isHasNext()} e peça a próxima página.
 */
@Value
public class PagedResult<T> {
    List<T> items;
    boolean hasNext;
    boolean hasPrevious;
    int page;
    int pageSize;
    long totalCount;
}
