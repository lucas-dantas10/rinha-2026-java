package br.com.rinha.fraud_detection.builder;

import br.com.rinha.fraud_detection.dto.TransactionVector;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jelmerk.knn.DistanceFunctions;
import com.github.jelmerk.knn.hnsw.HnswIndex;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class IndexBuilder {

    static final int VECTOR_DIMENSIONS = TransactionVector.DIMENSIONS;
    static final int HNSW_M = 16;
    static final int EF_CONSTRUCTION = 150;
    /** ef de busca; tunado para P99 < 1 ms com K=5 (≥ 2×K). */
    static final int SEARCH_EF = 40;
    static final double CAPACITY_MARGIN = 1.10;

    public static void main(String[] args) throws Exception {
        Path inputPath = args.length > 0 ? Path.of(args[0]) : null;
        Path outputPath = args.length > 1 ? Path.of(args[1]) : Path.of("references.idx");

        ObjectMapper mapper = new ObjectMapper();

        try (InputStream countStream = openInput(inputPath)) {
            CountResult count = countValidVectors(countStream, mapper);
            int maxElements = capacityFor(count.valid());

            System.out.printf(
                "Construindo índice: %d vetores válidos, capacidade=%d (ignorados: %d)%n",
                count.valid(),
                maxElements,
                count.skipped()
            );

            try (InputStream buildStream = openInput(inputPath)) {
                HnswIndex<String, float[], TransactionVector, Float> index = HnswIndex
                    .newBuilder(
                        VECTOR_DIMENSIONS,
                        DistanceFunctions.FLOAT_EUCLIDEAN_DISTANCE,
                        maxElements
                    )
                    .withM(HNSW_M)
                    .withEfConstruction(EF_CONSTRUCTION)
                    .withEf(SEARCH_EF)
                    .build();

                long added = populateIndex(buildStream, mapper, index);
                index.save(outputPath);

                long bytes = Files.size(outputPath);
                System.out.printf(
                    "Índice salvo em %s (%d itens, %d bytes)%n",
                    outputPath.toAbsolutePath(),
                    added,
                    bytes
                );
            }
        }
    }

    private static InputStream openInput(Path inputPath) throws Exception {
        if (inputPath != null) {
            return new BufferedInputStream(
                Files.newInputStream(inputPath),
                1024 * 64
            );
        }

        InputStream classpath = IndexBuilder.class
            .getClassLoader()
            .getResourceAsStream("references.json.gz");

        if (classpath == null) {
            throw new IllegalStateException(
                "references.json.gz não encontrado no classpath nem caminho informado"
            );
        }

        return new BufferedInputStream(classpath, 1024 * 64);
    }

    private record CountResult(long valid, long skipped) {}

    private static CountResult countValidVectors(
        InputStream rawInput,
        ObjectMapper mapper
    ) throws Exception {
        long valid = 0;
        long skipped = 0;

        try (
            GZIPInputStream gzipInput = new GZIPInputStream(rawInput);
            JsonParser parser = mapper.createParser(gzipInput)
        ) {
            requireArray(parser);

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                TransactionVector record = mapper.readValue(parser, TransactionVector.class);
                if (isValidVector(record)) {
                    valid++;
                } else {
                    skipped++;
                }
            }
        }

        return new CountResult(valid, skipped);
    }

    private static long populateIndex(
        InputStream rawInput,
        ObjectMapper mapper,
        HnswIndex<String, float[], TransactionVector, Float> index
    ) throws Exception {
        long count = 0;

        try (
            GZIPInputStream gzipInput = new GZIPInputStream(rawInput);
            JsonParser parser = mapper.createParser(gzipInput)
        ) {
            requireArray(parser);

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                TransactionVector vector = mapper.readValue(parser, TransactionVector.class);

                if (!isValidVector(vector)) {
                    continue;
                }

                index.add(withId(vector, count));
                count++;

                if (count % 100_000 == 0) {
                    System.out.println("Processados: " + count);
                }
            }
        }

        return count;
    }

    private static void requireArray(JsonParser parser) throws Exception {
        if (parser.nextToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("JSON inválido: esperado array na raiz");
        }
    }

    static boolean isValidVector(TransactionVector record) {
        return record != null
            && record.vector() != null
            && record.vector().length == VECTOR_DIMENSIONS;
    }

    private static TransactionVector withId(TransactionVector vector, long index) {
        if (vector.id() != null && !vector.id().isBlank()) {
            return vector;
        }
        return new TransactionVector("ref-" + index, vector.vector(), vector.label());
    }

    static int capacityFor(long validCount) {
        if (validCount <= 0) {
            throw new IllegalStateException("Nenhum vetor válido para indexar");
        }
        return Math.max(1, (int) Math.ceil(validCount * CAPACITY_MARGIN));
    }
}
