package ru.bmstu.iu9.dpdp_lab_1.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import ru.bmstu.iu9.dpdp_lab_1.model.Matrix;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class MatrixService implements CommandLineRunner {


    @Override
    public void run(String... args) {
        StopWatch stopWatch = new StopWatch();
        Matrix aMatrix = createRandomQuadraticMatrix(1800);
        Matrix bMatrix = createRandomQuadraticMatrix(1800);
        stopWatch.start();
        Matrix cMatrix = multiplexMatrixString(aMatrix, bMatrix);
        stopWatch.stop();
        log.info("Прошло времени строковом: " + stopWatch.getTotalTimeSeconds());
        stopWatch = new StopWatch();
        stopWatch.start();
        Matrix cMatrix2 = multiplexMatrixColumn(aMatrix, bMatrix);
        stopWatch.stop();
        log.info("Прошло времени столбцовом: " + stopWatch.getTotalTimeSeconds());
        assert cMatrix != null;
        for (int i = 1; i < 128; i++) {
            stopWatch = new StopWatch();
            stopWatch.start();
            Matrix cMatrix3 = parallelMatrixMultiplication(aMatrix, bMatrix, i);
            stopWatch.stop();
            log.info("Прошло времени паралельно "+i+": " + stopWatch.getTotalTimeSeconds());
//            log.info(String.valueOf(equalMatrix(cMatrix, cMatrix3)));
        }
    }

    private boolean equalMatrix(Matrix one, Matrix two) {
        for (int i = 0; i < one.getMatrix().length; i++) {
            for (int j = 0; j < one.getMatrix()[0].length; j++) {
                if (one.getMatrix()[i][j] != two.getMatrix()[i][j]) {
                    log.info(String.valueOf(one.getMatrix()[i][j]));
                    log.info(String.valueOf(two.getMatrix()[i][j]));
                    return false;
                }
            }
        }
        return true;
    }

    private Matrix multiplexMatrixColumn(Matrix one, Matrix two) {
        if (one.getMatrix().length == 0 || two.getMatrix().length == 0) {
            return null;
        }
        if (one.getMatrix()[0].length != two.getMatrix().length) {
            return null;
        }
        int mLength = one.getMatrix().length;
        int nLength = two.getMatrix()[0].length;
        int kLength = one.getMatrix()[0].length;
        int[][] matrix = new int[mLength][nLength];
        for (int j = 0; j < nLength; j++) {
            for (int i = 0; i < mLength; i++) {
                int Cij = 0;
                for (int k = 0; k < kLength; k++) {
                    int Aik = one.getMatrix()[i][k];
                    int Bkj = two.getMatrix()[k][j];
                    Cij += Aik * Bkj;
                }
                matrix[i][j] = Cij;
            }
        }
        return new Matrix(matrix);
    }

    private Matrix multiplexMatrixString(Matrix one, Matrix two) {
        if (one.getMatrix().length == 0 || two.getMatrix().length == 0) {
            return null;
        }
        if (one.getMatrix()[0].length != two.getMatrix().length) {
            return null;
        }
        int mLength = one.getMatrix().length;
        int nLength = two.getMatrix()[0].length;
        int kLength = one.getMatrix()[0].length;
        int[][] matrix = new int[mLength][nLength];
        for (int i = 0; i < mLength; i++) {
            for (int j = 0; j < nLength; j++) {
                int Cij = 0;
                for (int k = 0; k < kLength; k++) {
                    int Aik = one.getMatrix()[i][k];
                    int Bkj = two.getMatrix()[k][j];
                    Cij += Aik * Bkj;
                }
                matrix[i][j] = Cij;
            }
        }
        return new Matrix(matrix);
    }

    private Matrix createRandomQuadraticMatrix(Integer n) {
        Random random = new Random();
        int[][] matrix = new int[n][n];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = random.nextInt();
            }
        }
        return new Matrix(matrix);
    }


    public Matrix parallelMatrixMultiplication(Matrix aMatrix, Matrix bMatrix, int numThreads) {
        int[][] A = aMatrix.getMatrix();
        int[][] B = bMatrix.getMatrix();
        int n = A.length; // Количество строк в матрице A
        int p = B[0].length; // Количество столбцов в матрице B
        int[][] C = new int[n][p]; // Результирующая матрица

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        int chunkSize = n / numThreads;

        for (int i = 0; i < numThreads; i++) {
            final int startRow = i * chunkSize;
            final int endRow = (i == numThreads - 1) ? n : (i + 1) * chunkSize;
            executorService.execute(() -> {
                for (int row = startRow; row < endRow; row++) {
                    for (int col = 0; col < p; col++) {
                        int sum = 0;
                        for (int k = 0; k < A[0].length; k++) {
                            sum += A[row][k] * B[k][col];
                        }
                        C[row][col] = sum;
                    }
                }
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Matrix(C);
    }
}
