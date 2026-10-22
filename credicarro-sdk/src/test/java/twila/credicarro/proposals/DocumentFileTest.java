package twila.credicarro.proposals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import twila.credicarro.proposals.model.DocumentFile;

class DocumentFileTest {

    @Test
    void fromBytesEncodesPureBase64WithoutDataPrefix() {
        DocumentFile file = DocumentFile.fromBytes("%PDF-1.7".getBytes(StandardCharsets.UTF_8), "cnh-comprador.pdf");

        assertThat(file.getFileName()).isEqualTo("cnh-comprador.pdf");
        assertThat(file.getBase64Content()).isEqualTo(Base64.getEncoder().encodeToString("%PDF-1.7".getBytes(StandardCharsets.UTF_8)));
        assertThat(file.getBase64Content()).doesNotContain("data:").doesNotContain("\n");
    }

    @Test
    void fromStreamReadsTheWholeContent() {
        DocumentFile file = DocumentFile.fromStream(new ByteArrayInputStream(new byte[] {1, 2, 3}), "foto.PNG");

        assertThat(file.getBase64Content()).isEqualTo(Base64.getEncoder().encodeToString(new byte[] {1, 2, 3}));
    }

    @Test
    void fromFileUsesTheFileNameWithoutThePath(@TempDir Path dir) throws IOException {
        Path path = dir.resolve("comprovante.jpeg");
        Files.write(path, new byte[] {9, 9});

        DocumentFile file = DocumentFile.fromFile(path);

        assertThat(file.getFileName()).isEqualTo("comprovante.jpeg");
    }

    @Test
    void acceptsAllowedExtensionsCaseInsensitively() {
        for (String name : new String[] {"a.pdf", "a.jpg", "a.jpeg", "a.png", "A.PDF", "a.JpG"})
            assertThat(DocumentFile.fromBytes(new byte[] {1}, name).getFileName()).isEqualTo(name);
    }

    @Test
    void rejectsInvalidExtensions() {
        for (String name : new String[] {"a.exe", "a", "a.txt", "a.pdf.exe", "a.gif"})
            assertThatThrownBy(() -> DocumentFile.fromBytes(new byte[] {1}, name)).as(name).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNamesWithPathOrBlankOrTooLong() {
        assertThatThrownBy(() -> DocumentFile.fromBytes(new byte[] {1}, "pasta/a.pdf")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DocumentFile.fromBytes(new byte[] {1}, "pasta\\a.pdf")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DocumentFile.fromBytes(new byte[] {1}, " ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DocumentFile.fromBytes(new byte[] {1}, null)).isInstanceOf(IllegalArgumentException.class);

        String longName = new String(new char[252]).replace('\0', 'a') + ".pdf";
        assertThat(longName).hasSize(256);
        assertThatThrownBy(() -> DocumentFile.fromBytes(new byte[] {1}, longName)).isInstanceOf(IllegalArgumentException.class);

        String maxName = new String(new char[251]).replace('\0', 'a') + ".pdf";
        assertThat(DocumentFile.fromBytes(new byte[] {1}, maxName)).isNotNull();
    }

    @Test
    void enforcesTheSizeLimits() {
        assertThatThrownBy(() -> DocumentFile.fromBytes(new byte[0], "a.pdf")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DocumentFile.fromBytes(null, "a.pdf")).isInstanceOf(IllegalArgumentException.class);

        assertThat(DocumentFile.fromBytes(new byte[10 * 1024 * 1024], "a.pdf")).isNotNull();
        assertThatThrownBy(() -> DocumentFile.fromBytes(new byte[10 * 1024 * 1024 + 1], "a.pdf"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fromStreamStopsReadingOnceTheLimitIsExceeded() {
        ByteArrayInputStream endless = new ByteArrayInputStream(new byte[10 * 1024 * 1024 + 100]);

        assertThatThrownBy(() -> DocumentFile.fromStream(endless, "a.pdf")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toStringDoesNotExposeTheContent() {
        DocumentFile file = DocumentFile.fromBytes(new byte[] {1, 2, 3}, "a.pdf");

        assertThat(file.toString()).contains("a.pdf").doesNotContain(file.getBase64Content());
    }
}
