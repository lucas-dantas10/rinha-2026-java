package br.com.rinha.fraud_detection.service;

import br.com.rinha.fraud_detection.config.VectorIndexProperties;
import br.com.rinha.fraud_detection.dto.TransactionVector;
import com.github.jelmerk.knn.DistanceFunctions;
import com.github.jelmerk.knn.hnsw.HnswIndex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VectorIndexServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void loadAndSearch_returnsKNeighbors() throws Exception {
        Path indexPath = tempDir.resolve("test.idx");
        buildFixtureIndex(indexPath);

        VectorIndexProperties props = new VectorIndexProperties(
            indexPath.toString(),
            40,
            10,
            false
        );
        VectorIndexService service = new VectorIndexService(props);
        service.loadIndex(indexPath);

        assertTrue(service.isReady());

        float[] query = new float[14];
        for (int i = 0; i < query.length; i++) {
            query[i] = 0.5f;
        }

        List<TransactionVector> neighbors = service.search(query, 5);

        assertEquals(5, neighbors.size());
    }

    private static void buildFixtureIndex(Path indexPath) throws Exception {
        HnswIndex<String, float[], TransactionVector, Float> index = HnswIndex
            .newBuilder(14, DistanceFunctions.FLOAT_EUCLIDEAN_DISTANCE, 16)
            .withM(16)
            .withEfConstruction(100)
            .withEf(40)
            .build();

        for (int i = 0; i < 8; i++) {
            float[] vector = new float[14];
            vector[0] = i * 0.1f;
            index.add(new TransactionVector("id-" + i, vector, i % 2 == 0 ? "fraud" : "legit"));
        }

        index.save(indexPath);
    }
}
