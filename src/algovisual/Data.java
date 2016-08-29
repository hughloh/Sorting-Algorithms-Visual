/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algovisual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.Node;

final class Data implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger("algovisual.Data");
    private static final String DEF_NAME = "DataSet";
    
    public boolean report = false;
    private String name;
    private SortingAlgorithm algorithm;
    private final static LongProperty COST_MULTIPLIER = new SimpleLongProperty(100);
    private final static DoubleProperty SWAP_COST = new SimpleDoubleProperty(3), COMPARE_COST = new SimpleDoubleProperty(1), COPY_COST = new SimpleDoubleProperty(1);
    private final int[] arr;
    private Data temp;
    private int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
    private final transient ArrayList<Node> observers = new ArrayList<>();
    private int n_swaps = 0, n_compares = 0, n_copies = 0;
    private int focus_first = -1, focus_last = -1;
    
    private Data(int length, String name) {
        arr = new int[length];
        this.name = (name == null) ? DEF_NAME : name;
    }
    
    private Data(int length) {
        this(length, DEF_NAME);
    }
    
    /**
     * Creates and returns a Data object of size length.
     * Useful as temporary storage for algorithms.
     * @param length size of storage
     * @return new empty Data object with no attached algorithm.
     */
    static Data makeEmptyData(int length) {
        return new Data(length);
    }
    
    /**
     * Creates and returns a Data object representing the provided array of ints.
     * Creates and attaches a SortingAlgorithm object (created from the provided 
     * algofunc) to the returned Data object.
     * 
     * @param <T> extends SortingAlgorithm.
     * @param arr the array that the returned Data object is to represent.
     * @param algofunc any method that accepts a Data object as a parameter and returns a SortingAlgorithm object. Example: Quick::new
     *                  
     * @return new Data object representing the provided array, with attached SortingAlgorithm
     */
    static <T extends SortingAlgorithm> Data makeDataFrom(int[] arr, Function<Data, T> algofunc) {
        Data d = new Data(arr.length);
        if (algofunc != null) d.setAlgo(algofunc.apply(d));
        for (int i = 0; i < arr.length; i++) d.set(i, arr[i]);
        return d;
    }
    
    /**
     * Creates and returns a Data object representing a copy of the array found in the provided Data object.
     * Creates and attaches a SortingAlgorithm object (created from the provided algofunc) to the returned Data object.
     * @param <T>
     * @param d
     * @param algofunc
     * @return 
     */
    static <T extends SortingAlgorithm> Data makeDataFrom(Data d, Function<Data, T> algofunc) {
        return makeDataFrom(d.arr, algofunc);
    }
    
    /**
     * Creates and returns a Data object representing a copy of the array found in the provided Data object.
     * Retrieves algorithm constructor matching SortingAlgorithm.Catalog type.
     * @param d
     * @param algoType
     * @return 
     */
    static Data makeDataFrom(Data d, SortingAlgorithm.Catalog algoType) {
        return makeDataFrom(d.arr, algoType.algoConstructor() );
    }
    
    String getName() { return name; }
    void setName(String newName) { this.name = newName; }
    
    public static void setSwapCost(long cost)       { if (cost >= 0) SWAP_COST.setValue(cost);         }
    public static void setCompareCost(long cost)    { if (cost >= 0) COMPARE_COST.setValue(cost);      }
    public static void setCopyCost(long cost)       { if (cost >= 0) COPY_COST.setValue(cost);         }
    public static void setCostMultiplier(long cost) { if (cost >= 0) COST_MULTIPLIER.setValue(cost);   }
    public static DoubleProperty getSwapCost()      { return SWAP_COST;     }
    public static DoubleProperty getCompareCost()   { return COMPARE_COST;  }
    public static DoubleProperty getCopyCost()      { return COPY_COST;     }
    public static LongProperty getCostMultiplier()  { return COST_MULTIPLIER; }
    
    public int getSwapCount()     { return n_swaps;       }
    public int getCompareCount()  { return n_compares;    }
    public int getCopyCount()     { return n_copies;      }
    public int getFocusFirst()    { return focus_first > 0 ? focus_first : 0;   }
    public int getFocusLast()     { return focus_last > 0 ? focus_last : arr.length-1;    }
    
    void setAlgo(SortingAlgorithm algo)  { algorithm = algo; }
    SortingAlgorithm getAlgo()           { return algorithm; }
    
    void registerObserver(Node n) {
        observers.add(n);
    }
    
    private void fireEvent(AlgoEvent e) {
        observers.forEach(n -> n.fireEvent(e));
        if (report) System.out.println(e);
    }
    
    public void set(int index, int x) {
        arr[index] = x;
    }
    
    private synchronized void pause(long units) {
        if (units == 0 || COST_MULTIPLIER.getValue() == 0) return;
        try {
            this.wait(units * COST_MULTIPLIER.getValue());
        } catch (InterruptedException e) {
//            logger.info("Thread interrupted while Data was waiting.\n" + e);
            Thread.currentThread().interrupt();
        }
    }
    
    Data getTemp() {
        if (temp == null) {
            temp = makeEmptyData(algorithm.getTempStorageCost());
            temp.setMax(getMax());
        }
        return temp;
    }
    
    void focusRange(int first, int last) {
        focus_first = first;
        focus_last = last;
    }
    
    void focusAll() {
        focus_first = 0;
        focus_last = arr.length-1;
    }
    
    void focusNone() {
        focus_first = -1;
        focus_last = -1;
    }
    
    void swap(int i, int j) {
        n_swaps++;
        
        copyToTemp(i, 0);
        copy(j, i);
        copyFromTemp(0, j);
        
        LOGGER.finest("Swapping " + i + " and " + j);
    }
    
    synchronized int compare(int i, int j) {
        fireEvent(new AlgoEvent(AlgoEvent.COMPARE_EVENT, i, j));
        
        LOGGER.finest("Comparing " + i + " and " + j);
        
        pause(COMPARE_COST.longValue());
        
        n_compares++;
        
        return (arr[i] < arr[j]) ? -1 : (arr[i] == arr[j]) ? 0 : 1;
    }
    
    /**
     * Fires {@link AlgoEvent.FINISHED_EVENT} with parameters denoting (i) 
     * number of compares and (j) number of swaps taken.
     */
    void doneSorting() {
        focusNone();
        fireEvent(new AlgoEvent(AlgoEvent.FINISHED_EVENT, n_compares, n_copies, n_swaps));
    }

    
    private synchronized void copy(int from, int to) {
        fireEvent(new AlgoEvent(AlgoEvent.COPY_EVENT, to, from));
        pause(COPY_COST.longValue());
        arr[to] = arr[from];
        n_copies++;
    }
    
    
    synchronized void copyToTemp(int orig_i, int dest_i) {
        fireEvent(new AlgoEvent(AlgoEvent.COPY_EVENT, orig_i));
        temp.fireEvent(new AlgoEvent(AlgoEvent.COPY_EVENT, dest_i));
        pause(COPY_COST.longValue());
        temp.set(dest_i, arr[orig_i]);
        n_copies++;
        temp.n_copies++;
    }
    
    synchronized void copyFromTemp(int temp_i, int orig_i) {
        fireEvent(new AlgoEvent(AlgoEvent.COPY_EVENT, orig_i));
        temp.fireEvent(new AlgoEvent(AlgoEvent.COPY_EVENT, temp_i));
        pause(COPY_COST.longValue());
        set(orig_i, temp.arr[temp_i]);
        n_copies++;
        temp.n_copies++;
    }
    
    synchronized int get(int i) {
        return arr[i];
    }
    
    private int getMin() {
        if (min == Integer.MAX_VALUE) {
            min = arr[0];
            for (int i = 1; i < arr.length; i++) {
                if (arr[i] < min) min = arr[i];
            }
        }
        return min;
    }
    
    private void setMax(int max) { this.max = max; }
    
    private int getMax() {
        if (max == Integer.MIN_VALUE) {
            max = arr[0];
            for (int i = 1; i < arr.length; i++) {
                if (arr[i] > max) max = arr[i];
            }
        }
        return max;
    }
    
    double normalized(int index) {
        return (double)arr[index] / getMax();
    }
    
    int length() {
        return arr.length;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(arr[0]);
        for (int i = 1; i < arr.length; i++) {
            sb.append(" ").append(arr[i]);
        }
        return sb.toString();
    }
    
}

