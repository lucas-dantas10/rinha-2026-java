package br.com.rinha.fraud_detection.service;

import br.com.rinha.fraud_detection.config.VectorIndexProperties;
import br.com.rinha.fraud_detection.dto.TransactionVector;
import com.github.jelmerk.knn.SearchResult;
import com.github.jelmerk.knn.hnsw.HnswIndex;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class VectorIndexService {

    private static final Logger log = LoggerFactory.getLogger(VectorIndexService.class);
    private static final int WARMUP_K = 5;

    private final VectorIndexProperties properties;
    private final float[] warmupQuery = new float[TransactionVector.DIMENSIONS];

    private volatile HnswIndex<String, float[], TransactionVector, Float> index;
    private volatile boolean ready;

    public VectorIndexService(VectorIndexProperties properties) {
        this.properties = properties;
        for (int i = 0; i < warmupQuery.length; i++) {
            warmupQuery[i] = 0.5f;
        }
    }

    @PostConstruct
    void startLoading() {
        Path indexPath = Path.of(properties.path()).toAbsolutePath();

        if (!Files.isRegularFile(indexPath)) {
            throw new IllegalStateException(
                "Índice HNSW não encontrado em "
                    + indexPath
                    + ". Execute IndexBuilder e coloque references.idx no working dir."
            );
        }

        log.info(
            "Carregando índice HNSW em background (~2-3 min para ~3M vetores). "
                + "Tomcat sobe na porta 9999 em segundos; aguarde GET /ready retornar 200."
        );

        Thread loader = new Thread(() -> loadIndex(indexPath), "hnsw-index-loader");
        loader.setDaemon(false);
        loader.start();
    }

    void loadIndex(Path indexPath) {
        long start = System.currentTimeMillis();

        try {
            HnswIndex<String, float[], TransactionVector, Float> loaded =
                HnswIndex.load(indexPath);
            loaded.setEf(properties.searchEf());
            index = loaded;

            log.info(
                "Índice carregado em {} ms ({} itens, ef={}, path={})",
                System.currentTimeMillis() - start,
                index.size(),
                index.getEf(),
                indexPath
            );

            warmup();
            ready = true;
            log.info("API pronta para score de fraude");
        } catch (Exception ex) {
            log.error("Falha ao carregar índice HNSW em {}", indexPath, ex);
        }
    }

    private void warmup() {
        int iterations = properties.warmupIterations();
        long start = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            index.findNearest(warmupQuery, WARMUP_K);
        }

        log.info(
            "Warm-up HNSW concluído: {} buscas em {} ms",
            iterations,
            System.currentTimeMillis() - start
        );
    }

    public boolean isReady() {
        return ready;
    }

    public List<TransactionVector> search(float[] queryVector, int k) {
        if (!isReady()) {
            throw new IllegalStateException(
                "Índice HNSW ainda está carregando; tente novamente após GET /ready retornar 200"
            );
        }

        long start = properties.logSearchDuration()
            ? System.nanoTime()
            : 0L;

        List<SearchResult<TransactionVector, Float>> results =
            index.findNearest(queryVector, k);

        List<TransactionVector> neighbors = new ArrayList<>(results.size());

        for (SearchResult<TransactionVector, Float> result : results) {
            neighbors.add(result.item());
        }

        if (start > 0L) {
            log.debug(
                "findNearest k={} em {} µs",
                k,
                (System.nanoTime() - start) / 1_000
            );
        }

        return neighbors;
    }
}
