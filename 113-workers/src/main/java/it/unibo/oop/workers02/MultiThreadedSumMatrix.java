package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This implementation sum the values of a matrix.
 */
public class MultiThreadedSumMatrix implements SumMatrix {

    //Class Field
    private final int nthread;

    //Class Constructor

    /**
     * @param nthread the number of threads that we want to use.
     */
    public MultiThreadedSumMatrix(final int nthread) {
        this.nthread = nthread;
    }

    //Class Method

    /**
     * @InheritedDoc
     */
    @Override
    public double sum(final double[][] matrix) {
        final int matrixDimension = matrix.length * matrix[0].length;
        final int size = matrixDimension % this.nthread + matrixDimension / this.nthread;

       final List<Double> array = new ArrayList<>();

       for (final double[] value : matrix) {
            for (int y = 0; y < matrix[0].length; y++) {
                array.add(value[y]);
            }
       }

        final List<Worker> workers = new LinkedList<>();
        for (int start = 0; start < matrixDimension; start += size) {
            workers.add(new Worker(array, start, size));
        }

        for (final Worker worker : workers) {
            worker.start();
        }

        double sum = 0;
        for (final Worker worker : workers) {
            try {
                worker.join();
            } catch (final InterruptedException e) {
                System.out.println("Exception Generated -> " + e.getMessage()); //NOPMD
            }
            sum += worker.getSum();
        }
        return sum;
    }

    private static class Worker extends Thread {

        //Class Fields
        private final List<Double> array;
        private final int startpos;
        private final int nelem;
        private double res;

        //Class Constructor
        Worker(final List<Double> array, final int startPos, final int nElem) {
            this.array = array;
            this.startpos = startPos;
            this.nelem = nElem;
        }

        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public synchronized void run() {
                System.out.println("Working from index "
                    + this.startpos
                    + "to index"
                    + (this.startpos + this.nelem - 1)
                );
            for (int index = this.startpos; index < array.size() && index < this.startpos + this.nelem; index++) {
                this.res += array.get(index);
            }
        }

        public synchronized double getSum() {
            return this.res;
        }
    }
}
