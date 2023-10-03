package ru.bmstu.iu9.dpdp_lab_1.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Arrays;

@Getter
@AllArgsConstructor
public class Matrix {
    private int[][] matrix;

    @Override
    public String toString() {
        return Arrays.deepToString(matrix);
    }
}
