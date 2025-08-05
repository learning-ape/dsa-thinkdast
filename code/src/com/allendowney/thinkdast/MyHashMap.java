/**
 *
 */
package com.allendowney.thinkdast;

import java.util.List;
import java.util.Map;

/**
 * Implementation of a HashMap using a collection of MyLinearMap and
 * resizing when there are too many entries.
 *
 * @author downey
 * @param <K>
 * @param <V>
 *
 */
public class MyHashMap<K, V> extends MyBetterMap<K, V> implements Map<K, V> {

	// average number of entries per map before we rehash
	protected static final double FACTOR = 1.0;

	@Override
	public V put(K key, V value) {
		V oldValue = super.put(key, value);

		//System.out.println("Put " + key + " in " + map + " size now " + map.size());

		// check if the number of elements per map exceeds the threshold
		if (size() > maps.size() * FACTOR) {
			rehash();
		}
		return oldValue;
	}

	/**
	 * Doubles the number of maps and rehashes the existing entries.
	 */
	protected void rehash() {
		// TODO: FILL THIS IN!
		// save the existing entries
		List<MyLinearMap<K, V>> oldMaps = maps;

		// double the number of maps
		makeMaps(maps.size() * 2);

		// rehash the existing one: put the entries into the new map
		for (MyLinearMap<K,V> map : oldMaps) {
			for (Map.Entry<K,V> entry : map.getEntries()) {
				put(entry.getKey(), entry.getValue());
			}
		}
	}

	public void printHashMap() {
		for (int i = 0; i < maps.size(); i++) {
			System.out.print(i + ". List: ");
			for (Entry<K,V> entry : maps.get(i).getEntries()) {
				System.out.print(entry.getKey() + "->" + entry.getValue() + " ");
			}
			System.out.println();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, Integer> map = new MyHashMap<String, Integer>();
		for (int i=0; i<10; i++) {
			map.put(new Integer(i).toString(), i);
		}
		Integer value = map.get("3");
		System.out.println(value);
	}
}
