package br.com.rinha.fraud_detection.service;

import br.com.rinha.fraud_detection.dto.TransactionVector;
import com.github.jelmerk.knn.DistanceFunctions;
import com.github.jelmerk.knn.SearchResult;
import com.github.jelmerk.knn.hnsw.HnswIndex;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Service
public class VectorIndexService {

    private HnswIndex<String, float[], TransactionVector, Float> index;

    @PostConstruct
    public void init() throws Exception {
        List<TransactionVector> references =
            loadFromGzip("references.json.gz");

        if (references.isEmpty()) {
            throw new IllegalStateException(
                "Nenhum vetor encontrado"
            );
        }

        int dimensions =
            references.getFirst()
                .vector()
                .length;

        index = HnswIndex
            .newBuilder(
                dimensions,
                DistanceFunctions.FLOAT_EUCLIDEAN_DISTANCE,
                references.size()
            )
            .withM(16)
            .withEfConstruction(200)
            .withEf(50)
            .build();

        index.addAll(references);
    }

    public List<TransactionVector> search(
        float[] queryVector,
        int k
    ) {
        return index.findNearest(queryVector, k)
            .stream()
            .map(SearchResult::item)
            .toList();
    }

    private List<TransactionVector> loadFromGzip(
        String fileName
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        List<TransactionVector> vectors =
            new ArrayList<>();

        InputStream fileInput =
            getClass()
                .getClassLoader()
                .getResourceAsStream(fileName);

        if (fileInput == null) {
            throw new IllegalArgumentException(
                "Arquivo não encontrado: " + fileName
            );
        }

        try (
            BufferedInputStream bufferedInput =
                new BufferedInputStream(
                    fileInput,
                    1024 * 64
                );

            GZIPInputStream gzipInput =
                new GZIPInputStream(bufferedInput);

            JsonParser parser =
                mapper.createParser(gzipInput)
        ) {

            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException(
                    "JSON deve começar com um array"
                );
            }

            while (parser.nextToken() != JsonToken.END_ARRAY) {

                TransactionVector vector =
                    mapper.readValue(
                        parser,
                        TransactionVector.class
                    );

                vectors.add(vector);
            }
        }

        return vectors;
    }
}
