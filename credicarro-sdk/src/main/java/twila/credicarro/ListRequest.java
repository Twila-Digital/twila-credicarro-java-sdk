package twila.credicarro;

import lombok.Builder;
import lombok.Value;

/**
 * Paginação das listagens: {@code pagina_inicial} (mínimo 1) e {@code limite} (1 a 100). Padrão: página 1, 10 itens.
 */
@Value
public class ListRequest {

    public static final int MAX_PAGE_SIZE = 100;

    int page;
    int pageSize;

    @Builder
    private ListRequest(Integer page, Integer pageSize) {
        this.page = page != null ? page : 1;
        this.pageSize = pageSize != null ? pageSize : 10;

        if (this.page < 1)
            throw new IllegalArgumentException("page deve ser maior ou igual a 1.");

        if (this.pageSize < 1 || this.pageSize > MAX_PAGE_SIZE)
            throw new IllegalArgumentException("pageSize deve estar entre 1 e " + MAX_PAGE_SIZE + ".");
    }

    public static ListRequest of(int page, int pageSize) {
        return new ListRequest(page, pageSize);
    }

    public static ListRequest defaults() {
        return new ListRequest(null, null);
    }
}
