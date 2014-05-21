package ru.ifmo.ctddev.stoyanov.task2;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {

    private final List<E> array;
    private final List<E> reversedArray;
    private final Comparator<? super E> comparator;
    private boolean naturalOrdering = false;

    private ArraySet(Comparator<? super E> comparator, List<E> array, List<E> reversedArray) {
        this.array = array;
        this.comparator = comparator;
        this.reversedArray = reversedArray;
    }

    public ArraySet() {
        array = new ArrayList<>();
        reversedArray = new ArrayList<>();
        comparator = null;
    }

    public ArraySet(Collection<E> c) {
        this(c, new Comparator<E>() {
            @Override
            public int compare(E o1, E o2) {
                return ((Comparable<? super E>) o1).compareTo(o2);
            }
        });
        naturalOrdering = true;
    }

    @SuppressWarnings("unchecked")
    public ArraySet(Collection<E> c, Comparator<? super E> comparator) {
        List<E> temp = new ArrayList(c);
        List<E> ans = new ArrayList();
        Collections.sort(temp, comparator);
        for (int i = 0; i < temp.size(); i++) {
            if (i == 0 || comparator.compare(temp.get(i - 1), temp.get(i)) != 0) {
                ans.add(temp.get(i));
            }
        }

        array = ans;
        reversedArray = new ArrayList(array);
        Collections.reverse(reversedArray);
        this.comparator = comparator;
    }

    private E getOrNull(int a) {
        if (a < 0 || a >= array.size()) {
            return null;
        }
        return array.get(a);
    }

    private int binarySearch(E e) {
        return Collections.binarySearch(array, e, comparator);
    }

    @Override
    public E lower(E e) {
        int k = binarySearch(e);
        if (k >= 0) {
            return getOrNull(k - 1);
        } else {
            return getOrNull((-k - 1) - 1);
        }
    }

    @Override
    public E floor(E e) {
        int k = binarySearch(e);
        if (k >= 0) {
            return array.get(k);
        }
        return getOrNull((-k - 1) - 1);
    }

    @Override
    public E ceiling(E e) {
        int k = binarySearch(e);
        if (k >= 0) {
            return array.get(k);
        }
        return getOrNull(-k - 1);
    }

    @Override
    public E higher(E e) {
        int k = binarySearch(e);
        if (k >= 0) {
            return getOrNull(k + 1);
        } else {
            return getOrNull(-k - 1);
        }
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return array.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return (binarySearch((E) o) >= 0);
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            Iterator<E> iterator = array.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public E next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new Comparator<E>() {
            @Override
            public int compare(E o1, E o2) {
                return (-comparator.compare(o1, o2));
            }
        }, reversedArray, array);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return headSet(toElement, toInclusive).tailSet(fromElement, fromInclusive);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        int finish = binarySearch(toElement);
        if (finish >= 0) {
            if (!inclusive) {
                finish--;
            }
            finish++;
            if (finish < 0) {
                return new ArraySet<>(comparator, new ArrayList<E>(), new ArrayList<E>());
            }
            return new ArraySet<>(comparator, array.subList(0, finish), reversedArray.subList(array.size() - finish, array.size()));
        }
        return new ArraySet<>(comparator, array.subList(0, -finish - 1), reversedArray.subList(array.size() + finish + 1, array.size()));
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        int start = binarySearch(fromElement);
        if (start >= 0) {
            if (!inclusive) {
                start++;
            }
            if (start >= array.size()) {
                return new ArraySet<>(comparator, new ArrayList<E>(), new ArrayList<E>());
            }
            return new ArraySet<>(comparator, array.subList(start, array.size()), reversedArray.subList(0, array.size() - start));
        }
        return new ArraySet<>(comparator, array.subList(-start - 1, array.size()), reversedArray.subList(0, array.size() - (-start - 1)));
    }

    @Override
    public Comparator<? super E> comparator() {
        if (naturalOrdering) {
            return null;
        }
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public E first() {
        if (array.isEmpty()) {
            throw new NoSuchElementException();
        }
        return array.get(0);
    }

    @Override
    public E last() {
        if (array.isEmpty()) {
            throw new NoSuchElementException();
        }
        return array.get(array.size() - 1);
    }

}
