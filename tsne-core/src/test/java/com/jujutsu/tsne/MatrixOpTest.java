package com.jujutsu.tsne;

import com.jujutsu.utils.MatrixOps;
import com.jujutsu.utils.MatrixUtils;
import org.junit.Test;

import java.io.File;

import static com.jujutsu.utils.MatrixOps.parScalarMultiply;
import static com.jujutsu.utils.MatrixOps.scalarMultiply;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MatrixOpTest {
	
	@Test
	public void testPCA() {
        //double [][] nmatrix = MatrixUtils.simpleRead2DMatrix(new File("src/test/resources/datasets/nist_pca_2.txt"), " ");
       // System.out.println(ArrayString.printDoubleArray(X));
        //double [][] nmatrix = TSneDemo.nistReadStringDouble(ASCIIFile.read(new File("src/main/resources/datasets/mnist2500_X.txt")));
        double [][] matrix = MatrixOps.rnorm(200,7);
        System.out.println(MatrixOps.doubleArrayToPrintString(matrix));
        PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
        double [][] pcad = pca.pca(matrix, 2);
        System.out.println(MatrixOps.doubleArrayToPrintString(pcad));
	}
	
	@Test
	public void testTimeManyTransposes() {

		int rows = 15302;
		int cols = 1143;
		double [][] matrix = new double [rows][cols];
		double [][] trmatrix = new double [cols][rows];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				matrix[i][j] = (double) j + (i*cols);
				trmatrix[j][i] = (double) j + (i*cols);
			}
		}
		int noLaps = 4;
		long trtime = 0;
		long partrtime = 0;
		long time = 0;
		for (int laps = 0; laps < noLaps; laps++) {
			time = System.currentTimeMillis();
			double [][] tr1 = MatrixOps.transposeSerial(matrix);
			trtime += (System.currentTimeMillis()-time);
			assertEquals(cols, tr1.length);
			assertEquals(rows, tr1[0].length);
			time = System.currentTimeMillis();
			double [][] tr2 = MatrixOps.transpose(matrix,20);
			partrtime += (System.currentTimeMillis()-time);
			assertEquals(cols, tr2.length);
			assertEquals(rows, tr2[0].length);
			for (int i = 0; i < tr1.length; i++) {
                assertArrayEquals(trmatrix[i],tr1[i],0.0000001);
                assertArrayEquals(trmatrix[i],tr2[i],0.0000001);
			}
		}
		System.out.println("    Tr time: " + trtime);
		System.out.println("Par Tr time: " + partrtime);
	}

	
	@Test
	public void timeTransposesNist() {

		double [][] matrix = MatrixUtils.simpleRead2DMatrix(new File("src/test/resources/datasets/mnist2500_X.txt"), " ");
		int rows = matrix.length;
		int cols = matrix[0].length;
		double [][] trmatrix = new double [cols][rows];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				trmatrix[j][i] = matrix[i][j];
			}
		}
		int noLaps = 50;
		long trtime = 0;
		long partrtime = 0;
		long time = 0;
		System.out.println("Size is: " + rows + " x" + cols + "...");
		for (int laps = 0; laps < noLaps; laps++) {
			if((laps%10)==0) System.out.println("Iter " + laps + "...");
			time = System.currentTimeMillis();
			double [][] tr1 = MatrixOps.transposeSerial(matrix);
			trtime += (System.currentTimeMillis()-time);
			assertEquals(tr1.length,cols);
			assertEquals(tr1[0].length,rows);
			time = System.currentTimeMillis();
			double [][] tr2 = MatrixOps.transpose(matrix,20);
			partrtime += (System.currentTimeMillis()-time);
			assertEquals(tr2.length,cols);
			assertEquals(tr2[0].length,rows);
			for (int i = 0; i < tr1.length; i++) {
				assertArrayEquals(trmatrix[i], tr1[i], 0.0000001);
				assertArrayEquals(trmatrix[i], tr2[i], 0.0000001);
//				for (int j = 0; j < tr1[0].length; j++) {
//					assertEquals(trmatrix[i][j],tr1[i][j],0.0000001);
//					//assertEquals("I: " + i + " J:" + j, trmatrix[i][j],tr2[i][j],0.0000001);
//				}
			}
		}
		System.out.println("    Tr time: " + trtime);
		System.out.println("Par Tr time: " + partrtime);
	}
	
	@Test
	public void timeScalarMultNist() {
		double [][] matrix1 = MatrixUtils.simpleRead2DMatrix(new File("src/test/resources/datasets/mnist2500_X.txt"), " ");
		double [][] matrix2 = MatrixUtils.simpleRead2DMatrix(new File("src/test/resources/datasets/mnist2500_X.txt"), " ");
		int rows = matrix1.length;
		int cols = matrix1[0].length;
		int noLaps = 50;
		long trtime = 0;
		long partrtime = 0;
		long time = 0;
		System.out.println("Size is " + rows + " x " + cols + "...");
		for (int laps = 0; laps < noLaps; laps++) {
			if((laps%10)==0) System.out.println("Iter " + laps + "...");
			time = System.currentTimeMillis();

			double [][] tr1 = scalarMultiply(matrix1, matrix2);

			trtime += (System.currentTimeMillis()-time);
			time = System.currentTimeMillis();

			double [][] tr2 = parScalarMultiply(matrix1, matrix2);

			partrtime += (System.currentTimeMillis()-time);

			for (int i = 0; i < tr1.length; i++) {
				assertArrayEquals(tr1[i], tr2[i],0.0000001);
//				int tl = tr1[0].length;
//				for (int j = 0; j < tl; j++) {
//					assertEquals(//"I: " + i + " J:" + j,
//							tr1[i][j],tr2[i][j],0.0000001);
//				}
			}
		}
		System.out.println("    Tr time: " + trtime);
		System.out.println("Par Tr time: " + partrtime);
	}
	
	@Test
	public void testExtractRowFromFlatFirst() {
		double [] flatMatrix = {1,2,3,4,5,6,7,8,9,0};
		int dimension = 2;
		int rowIdx = 0;
		double [] row = MatrixOps.extractRowFromFlatMatrix(flatMatrix, rowIdx, dimension);
		double [] expected = {1, 2};
		assertArrayEquals(expected, row, 0.000000001);
	}

	
	@Test
	public void testExtractRowFromFlatMidRange() {
		double [] flatMatrix = {1,2,3,4,5,6,7,8,9,0};
		int dimension = 2;
		int rowIdx = 2;
		double [] row = MatrixOps.extractRowFromFlatMatrix(flatMatrix, rowIdx, dimension);
		double [] expected = {5, 6};
		assertArrayEquals(expected, row, 0.000000001);
	}
	
	@Test
	public void testExtractRowFromFlatLast() {
		double [] flatMatrix = {1,2,3,4,5,6,7,8,9,0};
		int dimension = 2;
		int rowIdx = 4;
		double [] row = MatrixOps.extractRowFromFlatMatrix(flatMatrix, rowIdx, dimension);
		double [] expected = {9, 0};
		assertArrayEquals(expected, row, 0.000000001);
	}


}
