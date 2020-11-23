package ScheduleQueue;

import java.net.URL;
import java.util.Queue;

public abstract class AbstractScheduleQueue implements ScheduleQueue {
    protected Queue<URL> queue;
    protected int maxCapacity = 3500;

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean addNewURL(URL url) {
        if(this.size() > maxCapacity){
            return false;
        }
        queue.add(url);
        return true;
    }

    @Override
    public URL nextURL() {
        return queue.poll();
    }
}
