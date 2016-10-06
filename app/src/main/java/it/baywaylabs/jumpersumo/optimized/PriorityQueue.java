package it.baywaylabs.jumpersumo.optimized;

/**
 * Created by Massimiliano on 28/03/2016.
 */

/**
 * This class implements a priority queue that fits into
 * the Java 1.2 Collection hierarchy. This is a min-based
 * priority queue though a max-based queue can be easily obtained
 * by supplying an alternative <code>Comparator</code> object when
 * the priority queue is constructed.
 * <P>
 * This implementation
 * supports O(log n) operations for all fundamental priority
 * queue operations: add and peek/remove;
 * the implementation uses a standard min-heap. There is no
 * restriction on inserting unique elements, so it is ok to insert
 * an element that is equal to an element already in the priority
 * queue. No guarantees are provided about which of two equal elements
 * is returned first by a peek/remove.
 * <P>
 * The elements in a priority queue must implement the
 * <code>Comparable</code> interface or else a
 * <code>Comparator</code> must be provided when the priority queue is
 * constructed.
 * <P>
 * For version 2.0: the methods have been renamed to be consistent
 * with the Queue interface and PriorityQueue implementation from
 * Java 5. This class should be drop-in/compatible with the Java
 * 5 priority queue except this class doesn't support generics. In
 * addition, this class does not implement methods <code>poll</code>
 * or <code>offer</code> from the Java 5 Queue interface. Instead,
 * it provides <code>add</code> and <code>remove</code>.
 * <P>
 * @author Owen Astrachan
 * @version 1.0, July 2000
 * @version 2.0, October 2004
 *
 */
import java.util.*;


public class PriorityQueue extends AbstractCollection
{
    private static class DefaultComparator implements Comparator
    {
        public int compare (Object o1, Object o2)
        {
            return ((Command) o1).getPriority() > ((Command) o2).getPriority() ? 1 : 0;
            //return ((Comparable) o1).compareTo(o2);
        }
    }
    private Comparator myComp = new DefaultComparator();
    private int        mySize;
    private ArrayList  myList;

    /**
     * This is a trivial iterator class that returns
     * elements in the PriorityQueue ArrayList field
     * one-at-a-time
     */
    private class PQItr implements Iterator
    {
        public Object next()
        {
            return myList.get(myCursor);
        }

        public boolean hasNext()
        {
            return myCursor <= mySize;
        }

        public void remove()
        {
            throw new UnsupportedOperationException("remove not implemented");
        }

        private int myCursor = 1;
    }



    /**
     * constructs an empty priority queue, when new elements
     * are added their natural order will be used to determine
     * which is minimal. This means elements that are added must
     * implement the <code>Comparable</code> interface and must
     * be <em>mutually comparable</em>, i.e.,
     * <code>e1.compareTo(e2)</code> must not throw a
     * <code>CastCastException</code> for any two elements <code>e1</code>
     * and <code>e2</code> in the priority queue.
     */

    public PriorityQueue()
    {
        myList = new ArrayList(32);
        myList.add(null);             // first slot has index 1
        mySize = 0;
    }

    /**
     * constructs an empty priority queue, when new elements
     * are added the <code>Comparator comp</code> determines which is
     * smaller.
     *
     * @param comp is the <code>Comparator</code> used in determining order
     */

    public PriorityQueue(Comparator comp)
    {
        this();
        myComp = comp;
    }

    /**
     * all elements in coll are added to the priority queue. The
     * complexity is O(n) where <code>coll.size() == n</code>
     *
     * @param coll is a collection of mutually comparable elements
     */

    public PriorityQueue(Collection coll)
    {
        this();
        myList.addAll(coll);
        mySize = coll.size();

        for(int k=coll.size()/2; k >= 1; k--)
        {
            heapify(k);
        }
    }

    /**
     * A new element <code>o</code> is added to the priority queue
     * in O(log n) time where n is the size of the priority queue.
     * <P>
     * The return value should be ignored, a boolean value must be
     * returned because of the requirements of the
     * <code>Collection</code> interface.
     *
     * @param o is the (Comparable) object added to the priority queue
     * @return true
     */

    public boolean add(Object o)
    {
        myList.add(o);        // stored, but not correct location
        mySize++;             // added element, update count
        int k = mySize;       // location of new element

        while (k > 1 && myComp.compare(myList.get(k/2), o) > 0)
        {
            myList.set(k, myList.get(k/2));
            k /= 2;
        }
        myList.set(k,o);

        return true;
    }

    /**
     * @return the number of elements in the priority queue
     */
    public int size()
    {
        return mySize;
    }

    /**
     * @return true if and only if the priority queue is empty
     */
    public boolean isEmpty()
    {
        return mySize == 0;
    }

    /**
     * The smallest/minimal element is removed and returned
     * in O(log n) time where n is the size of the priority queue.
     *
     * @return the smallest element (and removes it)
     */

    public Object remove()
    {
        if (! isEmpty())
        {
            Object hold = myList.get(1);

            myList.set(1, myList.get(mySize));  // move last to top
            myList.remove(mySize);              // pop last off
            mySize--;
            if (mySize > 1)
            {
                heapify(1);
            }
            return hold;
        }
        return null;
    }

    /**
     * Executes in O(1) time, returns smallest element
     * @return the minimal element in the priority queue
     */

    public Object peek()
    {
        return myList.get(1);
    }

    /**
     * Executes in O(1) time, returns biggest element
     * @return the biggest element in the priority queue
     */

    public Object lastPeek() { return myList.get(myList.size()-1); }

    /**
     * The order of the elements returned by the iterator is not specified
     * @return an iterator of all elements in priority queue
     */

    public Iterator iterator()
    {
        return new PQItr();
    }

    /**
     * works in O(log(size()-vroot)) time
     * @param vroot is the index at which re-heaping occurs
     * @precondition: subheaps of index vroot are heaps
     * @postcondition: heap rooted at index vroot is a heap
     */

    private void heapify(int vroot)
    {
        Object last = myList.get(vroot);
        int child, k = vroot;
        while (2*k <= mySize)
        {
            child = 2*k;
            if (child < mySize &&
                    myComp.compare(myList.get(child),
                            myList.get(child+1)) > 0)
            {
                child++;
            }
            if (myComp.compare(last, myList.get(child)) <= 0)
            {
                break;
            }
            else
            {
                myList.set(k, myList.get(child));
                k = child;
            }
        }
        myList.set(k, last);
    }


    /**
     * simple test harnass that inserts all arguments into a
     * priority queue and then removes them one at a time and prints
     * them one per line as they are removed
     * thus effectively sorting in O(n log n) time
     */

    public static void main(String args[])
    {
        List<Command> listaNodi = new ArrayList<Command>();
        Command c = new Command(2,"GO ON");
        listaNodi.add(c);
        //Command d = new Command(1,"GO BACK");
        //listaNodi.add(d);
        //Command e = new Command(7,"JUMP");
        //listaNodi.add(e);
        //Command f = new Command(4,"GO ON");
        //listaNodi.add(f);

        // PriorityQueue pq = new PriorityQueue(Array.asList(listaNodi));
        PriorityQueue pq = new PriorityQueue(listaNodi);

        while (pq.size() > 0)
        {
            System.out.println( ((Command)pq.peek()).getCmd() );
            pq.remove();
        }
    }
}