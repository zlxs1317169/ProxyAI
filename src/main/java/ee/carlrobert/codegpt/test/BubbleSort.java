package ee.carlrobert.codegpt.test;

import java.util.Arrays; // 导入 Arrays 工具类，用于简化数组打印

public class BubbleSort {

    /**
     * 实现冒泡排序算法。
     * 冒泡排序是一种简单的排序算法。它重复地遍历待排序的列表，
     * 比较每对相邻元素，如果它们的顺序错误就把它们交换过来。
     * 遍历列表的工作是重复进行的，直到没有再需要交换，也就是说该列表已经排序完成。
     *
     * @param arr 待排序的整数数组。
     */
    public static void bubbleSort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return; // 数组为空或只有一个元素时，无需排序
        }

        int n = arr.length;
        boolean swapped; // 标记在一次遍历中是否发生了交换

        // 外层循环控制遍历的次数，每次遍历将最大的元素“冒泡”到正确的位置
        for (int i = 0; i < n - 1; i++) {
            swapped = false; // 假设本轮没有发生交换

            // 内层循环进行相邻元素的比较和交换
            // n-1-i 是因为每次外层循环结束后，最大的 i 个元素已经排好序在数组的末尾
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    // 交换 arr[j] 和 arr[j+1]
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true; // 标记发生了交换
                }
            }

            // 如果在一轮遍历中没有发生任何交换，说明数组已经有序，可以提前结束
            if (!swapped) {
                break;
            }
        }
    }

    /**
     * 实现快速排序算法。
     * 快速排序是一种高效的、分治法的排序算法。它选择一个基准元素，将数组分成两个子数组，
     * 然后对这两个子数组进行递归排序。
     *
     * @param arr 待排序的整数数组。
     */
    public static void quickSort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return; // 如果数组为空或只有一个元素，无需排序
        }
        quickSortHelper(arr, 0, arr.length - 1);
    }

    private static void quickSortHelper(int[] arr, int low, int high) {
        if (low < high) {
            // pi 是分区索引，arr[pi] 已经排好序
            int pi = partition(arr, low, high);

            // 递归地对 pi 左侧和右侧的子数组进行排序
            quickSortHelper(arr, low, pi - 1);
            quickSortHelper(arr, pi + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        // 选择最后一个元素作为基准点
        int pivot = arr[high];
        int i = (low - 1); // 较小元素的索引

        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                // 交换 arr[i] 和 arr[j]
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }

        // 将基准点放到正确的位置
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1;
    }

    public static void main(String[] args) {
        int[] arr1 = {30, 5, 18, 2, 45, 10};
        System.out.print("Original array 1: ");
        printArray(arr1);
        bubbleSort(arr1);
        System.out.print("Sorted array 1 (bubble sort): ");
        printArray(arr1);

        int[] arr2 = {5, 1, 4, 2, 8};
        System.out.print("Original array 2: ");
        printArray(arr2);
        bubbleSort(arr2);
        System.out.print("Sorted array 2 (bubble sort): ");
        printArray(arr2);

        int[] arr3 = {1, 2, 3, 4, 5}; // 已经有序的数组
        System.out.print("Original array 3: ");
        printArray(arr3);
        bubbleSort(arr3);
        System.out.print("Sorted array 3 (bubble sort): ");
        printArray(arr3);

        int[] arr4 = {}; // 空数组
        System.out.print("Original array 4: ");
        printArray(arr4);
        bubbleSort(arr4);
        System.out.print("Sorted array 4 (bubble sort): ");
        printArray(arr4);

        int[] arr5 = {7}; // 单元素数组
        System.out.print("Original array 5: ");
        printArray(arr5);
        bubbleSort(arr5);
        System.out.print("Sorted array 5 (bubble sort): ");
        printArray(arr5);

        int[] arr6 = {30, 5, 18, 2, 45, 10};
        System.out.print("Original array 6: ");
        printArray(arr6);
        quickSort(arr6);
        System.out.print("Sorted array 6 (quick sort): ");
        printArray(arr6);
    }

    /**
     * 辅助方法：打印数组内容。
     * @param arr 待打印的数组。
     */
    private static void printArray(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println();
    }
}