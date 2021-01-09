// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.queue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FairQueue<T extends Queueable>
{
    private final List<T> list;
    private final Set<Long> set;
    
    public FairQueue() {
        this.list = new ArrayList<T>();
        this.set = new HashSet<Long>();
    }
    
    public int add(final T item) {
        int lastIndex;
        for (lastIndex = this.list.size() - 1; lastIndex > -1 && this.list.get(lastIndex).getIdentifier() != item.getIdentifier(); --lastIndex) {}
        ++lastIndex;
        this.set.clear();
        while (lastIndex < this.list.size() && !this.set.contains(this.list.get(lastIndex).getIdentifier())) {
            this.set.add(this.list.get(lastIndex).getIdentifier());
            ++lastIndex;
        }
        this.list.add(lastIndex, item);
        return lastIndex;
    }
    
    public void addAt(final int index, final T item) {
        if (index >= this.list.size()) {
            this.list.add(item);
        }
        else {
            this.list.add(index, item);
        }
    }
    
    public int size() {
        return this.list.size();
    }
    
    public T pull() {
        return this.list.remove(0);
    }
    
    public boolean isEmpty() {
        return this.list.isEmpty();
    }
    
    public List<T> getList() {
        return this.list;
    }
    
    public T get(final int index) {
        return this.list.get(index);
    }
    
    public T remove(final int index) {
        return this.list.remove(index);
    }
    
    public int removeAll(final long identifier) {
        int count = 0;
        for (int i = this.list.size() - 1; i >= 0; --i) {
            if (this.list.get(i).getIdentifier() == identifier) {
                this.list.remove(i);
                ++count;
            }
        }
        return count;
    }
    
    public void clear() {
        this.list.clear();
    }
    
    public int shuffle(final long identifier) {
        final List<Integer> iset = new ArrayList<Integer>();
        for (int i = 0; i < this.list.size(); ++i) {
            if (this.list.get(i).getIdentifier() == identifier) {
                iset.add(i);
            }
        }
        for (int j = 0; j < iset.size(); ++j) {
            final int first = iset.get(j);
            final int second = iset.get((int)(Math.random() * iset.size()));
            final T temp = this.list.get(first);
            this.list.set(first, this.list.get(second));
            this.list.set(second, temp);
        }
        return iset.size();
    }
    
    public void skip(final int number) {
        for (int i = 0; i < number; ++i) {
            this.list.remove(0);
        }
    }
    
    public T moveItem(final int from, final int to) {
        final T item = this.list.remove(from);
        this.list.add(to, item);
        return item;
    }
}
