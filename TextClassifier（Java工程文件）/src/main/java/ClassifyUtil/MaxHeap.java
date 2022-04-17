package ClassifyUtil;

import java.util.*;

//最大堆，用来K2筛选特征时使用
public class MaxHeap<E> implements Iterable<E>
{
    private PriorityQueue<E> queue;
    private int maxSize;

    public MaxHeap(int maxSize, Comparator<E> comparator)
    {
        if (maxSize <= 0)
            throw new IllegalArgumentException();
        this.maxSize = maxSize;
        this.queue = new PriorityQueue<E>(maxSize, comparator);
    }
    public boolean add(E e)
    {
        if (queue.size() < maxSize)
        { // 未达到最大容量，直接添加
            queue.add(e);
            return true;
        }
        else
        { // 队列已满
            E peek = queue.peek();
            if (queue.comparator().compare(e, peek) > 0)
            { // 将新元素与当前堆顶元素比较，保留较小的元素
                queue.poll();
                queue.add(e);
                return true;
            }
        }
        return false;
    }
    @Override
    public Iterator<E> iterator()
    {
        return queue.iterator();
    }

    public int size()
    {
        return queue.size();
    }
}
