/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algovisual;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

/**
 *
 * @author meng
 */
abstract class SortingAlgorithm implements Runnable {
    abstract String getName();
    abstract int getTempStorageCost();
    
    static enum Catalog {
        INSERTION("Insertion Sort", Insertion::new),
        SELECTION("Selection Sort", Selection::new),
        BUBBLE("Bubble Sort", Bubble::new),
        SHELL("Shell Sort", Shell::new),
        MERGE("Merge Sort", Merge::new),
        QUICK("Quick Sort", Quick::new)
        ;
        
        private final String name;
        private final Function<Data, SortingAlgorithm> algoFunc;
        
        Catalog(String name, Function<Data, SortingAlgorithm> algo) {
            this.name = name;
            this.algoFunc = algo;
        }
        
        public Function<Data, SortingAlgorithm> algoConstructor() {
            return algoFunc;
        }
        
        @Override public String toString() { return name; }
    }
}

class Insertion extends SortingAlgorithm {
    private final Data d;
    private static final String NAME = "Insertion";
    private static final int TEMP_STORAGE_COST = 1;
    
    public Insertion(Data d) {
        this.d = d;
    }
    
    @Override
    public void run() {
        Main:
        for (int i = 1; i < d.length(); i++) {
            for (int j = i; 
                    j > 0 && (d.compare(j, j-1) < 0); 
                    j--) 
            {
                d.swap(j, j-1);
                if (Thread.interrupted()) break Main;
            }
        }
        d.doneSorting();
    }
    
    @Override public String getName() { return NAME; }
    @Override public int getTempStorageCost() { return TEMP_STORAGE_COST; }
}

class Selection extends SortingAlgorithm {
    private final Data d;
    private static final String NAME = "Selection";
    private static final int TEMP_STORAGE_COST = 1;
    
    public Selection(Data d) {
        this.d = d;
    }
    
    @Override
    public void run() {
        Main:
        for (int i = 0; i < d.length(); i++) {
            int index_min = i;
            for (int j = i + 1; j < d.length(); j++) {
                if (d.compare(j, index_min) < 0) {
                    index_min = j;
                    if (Thread.interrupted()) break Main;
                }
            }
            if (index_min != i) d.swap(index_min, i);
        }
        d.doneSorting();
    }
    
    @Override public String getName() { return NAME; }
    @Override public int getTempStorageCost() { return TEMP_STORAGE_COST; }
}

class Bubble extends SortingAlgorithm {
    private final Data d;
    private static final String NAME = "Bubble";
    private static final int TEMP_STORAGE_COST = 1;
    
    public Bubble(Data d) {
        this.d = d;
    }
    @Override
    public void run() {
        boolean swapped = true;
        int end = d.length();
        Main:
        while (swapped) {
            swapped = false;
                for (int i = 1; i < end; i++) {
                    if (d.compare(i, i-1) < 0) {
                        d.swap(i, i-1);
                        swapped = true;
                    }
                    if (Thread.interrupted()) break Main;
                }
            end--;
        }
        d.doneSorting();
    }
    
    @Override public String getName() { return NAME; }
    @Override public int getTempStorageCost() { return TEMP_STORAGE_COST; }
}

class Shell extends SortingAlgorithm {
    private static final String NAME = "Shell";
    private final Data d;
    private static final Integer[] DEF_GAPS = {1, 4, 10, 23, 57, 132, 301, 701};
    private final Integer[] GAPS;
    private boolean isRunning = false;
    private static final int TEMP_STORAGE_COST = 1;
    
    public Shell(Data d) {
        this.d = d;
        this.GAPS = DEF_GAPS;
    }
    
    public Shell(Data d, Integer[] gaps) {
        this.d = d;
        this.GAPS = gaps;
    }
    
    @Override
    public void run() {
        isRunning = true;
        Arrays.sort(GAPS, Collections.reverseOrder());
        Main:
        for (int gap : GAPS) {
            for (int i = gap; i < d.length(); i++) {
                for (int j = i; j >= gap && d.compare(j, j-gap) < 0; j -= gap) {
                    d.swap(j, j-gap);
                    if (Thread.interrupted()) break Main;
                }
            }
        }
        d.doneSorting();
        isRunning = false;
    }
    
    public void setGaps(String gaps) {
        //TODO
    }
    
    @Override public String getName() { return NAME; }
    @Override public int getTempStorageCost() { return TEMP_STORAGE_COST; }
}

class Merge extends SortingAlgorithm {
    private static final String NAME = "Merge";
    private Data d;
    private int temp_storage_cost;
    
    public Merge(Data d) {
        this.d = d;
        temp_storage_cost = d.length();
//        Data.makeEmptyData(d.length());
    }
    
    @Override
    public void run() {
        mergesort(0, d.length()-1);
        d.doneSorting();
    }
    
    int recursion_count = 0;
    private void mergesort(int start_i, int end_i) {
        if (start_i < end_i) {
            int middle = (start_i + end_i) / 2;
            mergesort(start_i, middle);
            mergesort(middle+1, end_i);
            merge(start_i, middle+1, end_i);
        }
    }
    
    private void merge(int left, int right, int rightEnd) {
        d.focusRange(left, rightEnd);
        int orig_left = left;
        int orig_rightEnd = rightEnd;
        
        int temp_index = left;
        int left_end = right - 1;
        
        while (left <= left_end && right <= rightEnd) {
            int source_i = (d.compare(right, left) < 0) ? right++ : left++;
            d.copyToTemp(source_i, temp_index++);
        }
        while (right <= rightEnd) d.copyToTemp(right++, temp_index++);
        while (left <= left_end) d.copyToTemp(left++, temp_index++);
        
        while (orig_left <= orig_rightEnd) d.copyFromTemp(orig_left, orig_left++);
    }
    
    @Override public String getName() { return NAME; }
    @Override public int getTempStorageCost() { return temp_storage_cost; }
}

class Heap extends SortingAlgorithm {
    private static final String NAME = "Heap";
    private static final int TEMP_STORAGE_COST = 1;
    private final Data d;
    
    public Heap(Data d) {
        this.d = d;
    }
    
    public void run() {
        // heapify
        for (int i = d.length() - 1; i >= 0; i--) {
            sink(i, d.length() - 1);
        }
        
        // sort
        int last = d.length() - 1;
        while (last > 0) {
            d.swap(0, last);
            last--;
            sink(0, last);
        }
        
        d.doneSorting();
    }
    
    private void sink(int start, int end) {
        while (iChildLeft(start) <= end) {
            int child = iChildLeft(start);
            int swap = start;
            
            if (d.compare(swap, child) < 0) swap = child;
            if (child + 1 <= end && d.compare(swap, child+1) < 0) swap = child + 1;
            if (swap == start) return;
            else {
                d.swap(start, swap);
                start = swap;
            }
        }
    }
    
    private int iParent(int nodeIndex) {
        return (nodeIndex - 1) / 2;
    }
    
    private int iChildLeft(int nodeIndex) {
        return 2 * nodeIndex + 1;
    }
    
    private int iChildRight(int nodeIndex) {
        return 2 * nodeIndex + 2;
    }
    
    @Override public String getName() { return NAME; }
    @Override public int getTempStorageCost() { return TEMP_STORAGE_COST; }
}

class Quick extends SortingAlgorithm {
    private static final String NAME = "Quick";
    private static final int TEMP_STORAGE_COST = 1;
    private final Data d;
    
    public Quick(Data d) { this.d = d; }
    
    @Override
    public void run() {
        quicksort(0, d.length()-1);
        d.doneSorting();
    }
    
    private void quicksort(int lo, int hi) {
        if (lo < hi) {
            int p = partition(lo, hi);
            quicksort(lo, p-1);
            quicksort(p+1, hi);
        }
    }
    
    private int partition(int lo, int hi) {
        d.focusRange(lo, hi);
        int i_pivot = lo;
        while (lo < hi) {
            while ( lo <= hi  && d.compare(lo, i_pivot) <= 0 ) ++lo;
            while ( hi >= lo  && d.compare(hi, i_pivot) >= 0 ) --hi;
            if (lo < hi) d.swap(lo, hi);
        }
        if (i_pivot != lo) d.swap(i_pivot, hi);
        return hi;
    }
    
    @Override
    public String getName() { return NAME; }

    @Override
    public int getTempStorageCost() { return TEMP_STORAGE_COST; }
}

class Test {
    public static void main(String[] args) throws InterruptedException {
//        Handler console = new ConsoleHandler();
//        Logger logger = Logger.getLogger("algovisual.Data");
//        console.setLevel(Level.ALL);
//        logger.addHandler(console);
//        logger.setLevel(Level.ALL);
        

        Data.setCostMultiplier(0);
        
        Data d1 = Data.makeDataFrom(IntGenerator.randomIntsArray(20, 0, 99), Merge::new);
        Data d2 = Data.makeDataFrom(d1, Quick::new);
        
        System.out.println("d1: " + d1);
        System.out.println("d2: " + d2);
//        SortingAlgorithm ins = new Selection(d1);
//        SortingAlgorithm sel = new Insertion(d2);
        Thread t1 = new Thread(d1.getAlgo());
        Thread t2 = new Thread(d2.getAlgo());
        
        long start = System.currentTimeMillis();
        t1.start();
        t1.join();
        long end = System.currentTimeMillis();
        System.out.println(d1.getAlgo().getName() + " sort took " + (end - start) + "ms");
        
        start = System.currentTimeMillis();
        t2.start();
        t2.join();
        end = System.currentTimeMillis();
        System.out.println(d2.getAlgo().getName() + " sort took " + (end - start) + "ms");
        
        
        System.out.println("d1: " + d1);
        System.out.println("d2: " + d2);
    }

}