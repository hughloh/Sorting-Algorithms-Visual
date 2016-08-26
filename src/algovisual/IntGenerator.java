/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algovisual;

import java.util.Random;

/**
 *
 * @author meng
 */
class IntGenerator {
    private static final Random R = new Random(System.currentTimeMillis());
    
    /**
     * Utility method. Creates and returns an array of random, normal distributed ints.
     * @param length size of array
     * @param lowerLimit lower bound of generated integers (inclusive)
     * @param upperLimitEx upper bound of generated integers (exclusive)
     * @return 
     */
    static int[] randomIntsArray(int length, int lowerLimit, int upperLimit) {
        return R.ints(length, lowerLimit, upperLimit+1).toArray();
    }
    
    static int[] evenDistributionIntsArray(int length, int lowerLimit, int upperLimit) {
        int[] ints = new int[length];
        int diff = upperLimit + 1 - lowerLimit;
        for (int i = 0; i < length; i++) {
            ints[i] = lowerLimit + (i * diff / length);
        }
        return ints;
    }
    
    static int[] reverse(int[] arr) {
        int[] result = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[arr.length - 1 - i];
        }
        return result;
    }
    
    static int[] shuffle(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            swap(arr, i, R.nextInt(arr.length));
        }
        return arr;
    }
    
    static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
    
    static int[] almostSortedIntsArray(int length, int lowerLimit, int upperLimitEx, int messFrequency, int messFactor) {
        int[] ints = new int[length];
        int range = upperLimitEx - lowerLimit;
        
        for (int i = 0; i < length; i++) {
            double mod = (double) i / length * range;
            ints[i] = lowerLimit + (int)mod;
        }
        
        int step = length / messFrequency;
        
        for (int i = step; i < length; i += step) {
            if (i + messFactor < length) {
                int tmp = ints[i];
                int randomMessFactor = messFactor/2 - R.nextInt(messFactor);
                int swapIndex = i + randomMessFactor;
                ints[i] = ints[swapIndex];
                ints[swapIndex] = tmp;
            }
        }
        return ints;
    }
    
    static String toString(final int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i : arr) sb.append(i).append(" ");
        return sb.toString();
    }
    
    public static void main(String[] args) {
        System.out.println(IntGenerator.toString(IntGenerator.shuffle(evenDistributionIntsArray(20, 0, 5))));
        System.out.println(IntGenerator.toString(almostSortedIntsArray(20, 0, 100, 5, 1)));
        System.out.println(IntGenerator.toString(reverse(almostSortedIntsArray(20, 0, 100, 5, 1))));
    }
}
