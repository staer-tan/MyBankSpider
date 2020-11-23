package ScheduleQueue;

import java.net.URL;

/**
 * 任务队列，用于返回当前爬取的URL和调度下一个需爬取URL地址
 *
 *  由于接口无法控制构造器，故ScheduledQueue实现类必须拥有的构造器：
 *  1. 无参构造器,使用默认的Queue实现类LinkedList
 *  2. 传入一个Queue实现类参数作为调度器使用的队列数据结构
 */

public interface ScheduleQueue {

    // 队列大小，用于返回当前队列大小
    int size();

    // 向队列添加地址
    boolean addNewURL(URL url);

    // 返回队列中下一个URL
    URL nextURL();
}
