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
                // 如果当前元素大于下一个元素，则交换它们
                if (arr[j] > arr[j + 1]) {
                    // 交换 arr[j] 和 arr[j+1]
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true; // 发生了交换
                }
            }

            // 如果在一轮遍历中没有发生任何交换，说明数组已经有序，可以提前结束
            if (!swapped) {
                break;
            }
        }
    }

    public static void main(String[] args) {

        int[] arr1 = {30, 5, 18, 2, 45, 10};
        System.out.print("Original array 1: ");
        printArray(arr1);
        bubbleSort(arr1);
        System.out.print("Sorted array 1: ");
        printArray(arr1);

        int[] arr2 = {5, 1, 4, 2, 8};
        System.out.print("Original array 2: ");
        printArray(arr2);
        bubbleSort(arr2);
        System.out.print("Sorted array 2: ");
        printArray(arr2);

        int[] arr3 = {1, 2, 3, 4, 5}; // 已经有序的数组
        System.out.print("Original array 3: ");
        printArray(arr3);
        bubbleSort(arr3);
        System.out.print("Sorted array 3: ");
        printArray(arr3);

        int[] arr4 = {}; // 空数组
        System.out.print("Original array 4: ");
        printArray(arr4);
        bubbleSort(arr4);
        System.out.print("Sorted array 4: ");
        printArray(arr4);

        int[] arr5 = {7}; // 单元素数组
        System.out.print("Original array 5: ");
        printArray(arr5);
        bubbleSort(arr5);
        System.out.print("Sorted array 5: ");
        printArray(arr5);
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