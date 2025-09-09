package com.allendowney.thinkdast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Provides sorting algorithms.
 *
 */
public class ListSorter<T> {

	/**
	 * Sorts a list using a Comparator object.
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public void insertionSort(List<T> list, Comparator<T> comparator) {
	
		for (int i=1; i < list.size(); i++) {
			T elt_i = list.get(i);
			int j = i;
			while (j > 0) {
				T elt_j = list.get(j-1);
				if (comparator.compare(elt_i, elt_j) >= 0) {
					break;
				}
				list.set(j, elt_j);
				j--;
			}
			list.set(j, elt_i);
		}
	}

	/**
	 * Sorts a list using a Comparator object.
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public void mergeSortInPlace(List<T> list, Comparator<T> comparator) {
		List<T> sorted = mergeSort(list, comparator);
		list.clear();
		list.addAll(sorted);
	}

	/**
	 * Sorts a list using a Comparator object.
	 * 
	 * Returns a list that might be new.
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public List<T> mergeSort(List<T> list, Comparator<T> comparator) {
		int size = list.size();
		if (size == 1) {	// base case
			return list;
		}

		// 1. split the list in half
		int mid = size / 2;
		List<T> sub1 = list.subList(0, mid);
		List<T> sub2 = list.subList(mid, size);

		// 2. sort the halves
		List<T> left = mergeSort(sub1, comparator);
		List<T> right = mergeSort(sub2, comparator);

		// 3. merge them into a complete sorted list
		return merge(left, right, comparator);
	}

	/**
	 * Merges two sorted lists into a single sorted list.
	 * 
	 * @param first
	 * @param second
	 * @param comparator
	 * @return
	 */
	private List<T> merge(List<T> first, List<T> second, Comparator<T> comparator) {
		int length = first.size() + second.size();
		List<T> tempList = new ArrayList<>(length);

		int i = 0;
		int j = 0;

		for (int k = 0; k < length; k++) {
			if (i == first.size()) {
				tempList.add(second.get(j++));
			} else if (j == second.size()) {
				tempList.add(first.get(i++));
			} else {
				if (comparator.compare(first.get(i), second.get(j)) < 0) {
					tempList.add(first.get(i++));
				} else {
					tempList.add(second.get(j++));
				}
			}
		}

		return tempList;
	}

	/**
	 * Returns the list with the smaller first element, according to `comparator`.
	 * 
	 * If either list is empty, `pickWinner` returns the other.
	 * 
	 * @param first
	 * @param second
	 * @param comparator
	 * @return
	 */
	private List<T> pickWinner(List<T> first, List<T> second, Comparator<T> comparator) {
		if (first.size() == 0) {
			return second;
		}
		if (second.size() == 0) {
			return first;
		}
		int res = comparator.compare(first.get(0), second.get(0));
		if (res < 0) {
			return first;
		}
		if (res > 0) {
			return second;
		}
		return first;
	}


	/**
	 * Sorts a list. (binary system version using bit manipulation)
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public void radixSort(List<Integer> numbers) {
		System.out.println("unsorted: " + numbers);
		System.out.println();

		int radix = 2;
		LinkedList<Integer>[] buckets = makeLinkedList(radix);
		
		// outer: loop each digit (from last digit as 1st)
		int maxWidth = findMaxDigitWidth(numbers);
		for (int pos = 0; pos < maxWidth; pos++) {
			System.out.println("sorted by " + (int) Math.pow(radix, pos) + "s digit: ");

			// inner1: takes the data from the array and puts it on the lists
			for (Integer num : numbers) {
				// decimal system: division and modulo are expensive CPU operations
				// int temp = num / (int) Math.pow(10, pos);
				// int digit = temp % 10;

				// binary system: bit manipulation (bitwise operations are extremely cheap)
				int digit = (num >> pos) & 1;	
				System.out.println("\tof " + num + "(" + Integer.toBinaryString(num)+ "): " + digit);
				buckets[digit].add(num);
			}
			System.out.println(Arrays.toString(buckets));

			// inner2: copies it from the lists back to the array
			numbers.clear();
			for (LinkedList<Integer> bucket : buckets) {
				numbers.addAll(bucket);
				bucket.clear();
			}

			System.out.println("sorted: " + numbers);
			System.out.println();
		}
	}

	private Integer findMaxDigitWidth(List<Integer> numbers) {
		int max = Collections.max(numbers);
		// return String.valueOf(max).length();
		// return (int)(Math.log(max) / Math.log(2)) + 1; 			// option 1. log2(max)
		return Integer.SIZE - Integer.numberOfLeadingZeros(max); 	// option 2. bitwise trick
	}

	@SuppressWarnings("unchecked")
	private LinkedList<Integer>[] makeLinkedList(int radix) {
		LinkedList<Integer>[] array = (LinkedList<Integer>[]) new LinkedList[radix];
		for (int i = 0; i < array.length; i++) {
			array[i] = new LinkedList<>();
		}
		return array;
	}

	/**
	 * Sorts a list using a Comparator object.
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public void heapSort(List<T> list, Comparator<T> comparator) {
		PriorityQueue<T> heap = new PriorityQueue<T>(list.size(), comparator); // ascending-priority queue
		heap.addAll(list);

		for (int i = 0; !heap.isEmpty(); i++) {
			list.set(i, heap.poll());
		}
	}

	
	/**
	 * Returns the largest `k` elements in `list` in ascending order. (bounded heap)
	 * 
	 * @param k
	 * @param list
	 * @param comparator
	 * @return 
	 * @return
	 */
	public List<T> topK(int k, List<T> list, Comparator<T> comparator) {
		PriorityQueue<T> heap = new PriorityQueue<T>(k, comparator);

		for (T element : list) {
			if (heap.size() < k) {	// i. heap is not full
				heap.offer(element);
				continue;
			}
			// ii. haep is full
			int cmp = comparator.compare(element, heap.peek());
			if (cmp > 0) {	// greater than the smallest: can be one of the largest k
				heap.poll();
				heap.offer(element);
			}
			// smaller than the smallest: cannot be one of the largest k, just discard it
		}

		List<T> result = new ArrayList<T>(k);
		while (!heap.isEmpty()) {
			result.add(heap.poll());
		}
		return result;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<Integer> list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		
		Comparator<Integer> comparator = new Comparator<Integer>() {
			@Override
			public int compare(Integer elt1, Integer elt2) {
				return elt1.compareTo(elt2);
			}
		};
		
		ListSorter<Integer> sorter = new ListSorter<Integer>();
		sorter.insertionSort(list, comparator);
		System.out.println(list);

		list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		sorter.mergeSortInPlace(list, comparator);
		System.out.println(list);

		list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		sorter.heapSort(list, comparator);
		System.out.println(list);
	
		list = new ArrayList<Integer>(Arrays.asList(6, 3, 5, 8, 1, 4, 2, 7));
		List<Integer> queue = sorter.topK(4, list, comparator);
		System.out.println(queue);

		System.out.println();
		list = new ArrayList<Integer>(Arrays.asList(421, 240, 35, 532, 305, 430, 124));
		sorter.radixSort(list);
		System.out.println("result: " + list);

		list = new ArrayList<Integer>(Arrays.asList(5, 3, 7, 2));
		sorter.radixSort(list);
	}
}
