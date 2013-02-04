package org.pm4j.common.selection;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class EmptySelection<T> implements Selection<T> {

	private static final long serialVersionUID = 1L;

	public static final Selection<Object> EMPTY_OBJECT_SELECTION = new EmptySelection<Object>();

	/**
	 * @return an empty selection instance.
	 */
	@SuppressWarnings("unchecked")
	public static final <T_ITEM> Selection<T_ITEM> getEmptySelection() {
		return (Selection<T_ITEM>) EmptySelection.EMPTY_OBJECT_SELECTION;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public T next() {
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/** Block size has no effect on this iterator implementation. */
	@Override
	public void setIteratorBlockSizeHint(int readBlockSize) {
	}

	@Override
	public long getSize() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
	  return true;
	}

	@Override
	public boolean contains(T item) {
		return false;
	}
}
