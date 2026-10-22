package twila.credicarro.proposals.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Arquivo de documento para {@link ProposalDocumentUpload}. Valida as regras da API antes de enviar: nome sem caminho com
 * até 255 caracteres, extensão {@code .pdf}, {@code .jpg}, {@code .jpeg} ou {@code .png}, e de 1 byte a 10 MiB.
 * O conteúdo é materializado uma vez só, como Base64 puro (sem prefixo {@code data:}), e reutilizado em todas as
 * tentativas. A API também confere se a extensão corresponde ao conteúdo real do arquivo.
 */
public final class DocumentFile {

    public static final int MAX_FILE_NAME_LENGTH = 255;
    public static final int MAX_CONTENT_BYTES = 10 * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(".pdf", ".jpg", ".jpeg", ".png")));

    private final String fileName;
    private final String base64Content;

    private DocumentFile(String fileName, String base64Content) {
        this.fileName = fileName;
        this.base64Content = base64Content;
    }

    public String getFileName() {
        return fileName;
    }

    public String getBase64Content() {
        return base64Content;
    }

    /**
     * Conteúdo primeiro, depois o nome do arquivo.
     */
    public static DocumentFile fromBytes(byte[] content, String fileName) {
        validateFileName(fileName);

        if (content == null || content.length == 0)
            throw new IllegalArgumentException("O arquivo do documento está vazio.");

        if (content.length > MAX_CONTENT_BYTES)
            throw new IllegalArgumentException("O arquivo do documento excede o limite de 10 MiB.");

        return new DocumentFile(fileName, Base64.getEncoder().encodeToString(content));
    }

    /**
     * Lê em laço até {@code MAX + 1} bytes: nunca confia no tamanho informado pela origem.
     */
    public static DocumentFile fromStream(InputStream content, String fileName) {
        validateFileName(fileName);

        if (content == null)
            throw new IllegalArgumentException("O conteúdo do documento é obrigatório.");

        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] chunk = new byte[8192];
            int remaining = MAX_CONTENT_BYTES + 1;

            while (remaining > 0) {
                int read = content.read(chunk, 0, Math.min(chunk.length, remaining));
                if (read == -1)
                    break;

                buffer.write(chunk, 0, read);
                remaining -= read;
            }

            if (buffer.size() > MAX_CONTENT_BYTES)
                throw new IllegalArgumentException("O arquivo do documento excede o limite de 10 MiB.");

            return fromBytes(buffer.toByteArray(), fileName);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Lê o arquivo do disco (em laço, com limite) usando o nome do próprio arquivo, sem o caminho, como {@code nomeArquivo}.
     */
    public static DocumentFile fromFile(Path path) {
        String name = path.getFileName().toString();
        validateFileName(name);

        try (InputStream input = Files.newInputStream(path)) {
            return fromStream(input, name);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty())
            throw new IllegalArgumentException("O nome do arquivo é obrigatório.");

        if (fileName.length() > MAX_FILE_NAME_LENGTH)
            throw new IllegalArgumentException("O nome do arquivo deve ter no máximo 255 caracteres.");

        if (fileName.indexOf('/') >= 0 || fileName.indexOf('\\') >= 0)
            throw new IllegalArgumentException("O nome do arquivo não pode conter caminho.");

        int dot = fileName.lastIndexOf('.');
        String extension = dot >= 0 ? fileName.substring(dot).toLowerCase(Locale.ROOT) : "";
        if (!ALLOWED_EXTENSIONS.contains(extension))
            throw new IllegalArgumentException("A extensão do arquivo deve ser .pdf, .jpg, .jpeg ou .png.");
    }

    @Override
    public String toString() {
        return "DocumentFile[fileName=" + fileName + ", content=***]";
    }
}
