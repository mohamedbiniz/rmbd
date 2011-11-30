package at.ainf.queryselection;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 31.05.11
 * Time: 15:41
 * To change this template use File | Settings | File Templates.
 */
public class BubbleSort<T> {

    private Comparator<T> comparator;

    public BubbleSort(){

    }

    public BubbleSort(Comparator<T> comp){
        this.comparator = comp;
    }


    public void sort(T[] queryModuleDiags){
         boolean unsorted=true;
         T temp;

          while (unsorted){
             unsorted = false;
             for (int i=0; i < queryModuleDiags.length-1; i++)
                if (comparator.compare(queryModuleDiags[i],queryModuleDiags[i+1]) < 0) {
                   temp       = queryModuleDiags[i];
                   queryModuleDiags[i]       = queryModuleDiags[i+1];
                   queryModuleDiags[i+1]     = temp;
                   unsorted = true;
                }
          }
    }

    public static void sort(double[] x) {
          boolean unsorted=true;
          double temp;

          while (unsorted){
             unsorted = false;
             for (int i=0; i < x.length-1; i++)
                if (x[i] > x[i+1]) {
                   temp       = x[i];
                   x[i]       = x[i+1];
                   x[i+1]     = temp;
                   unsorted = true;
                }
          }
    }

    public static void sort(int[] x) {
          boolean unsorted=true;
          int temp;

          while (unsorted){
             unsorted = false;
             for (int i=0; i < x.length-1; i++)
                if (x[i] > x[i+1]) {
                   temp       = x[i];
                   x[i]       = x[i+1];
                   x[i+1]     = temp;
                   unsorted = true;
                }
          }
    }




}

    

