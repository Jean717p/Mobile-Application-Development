package com.mad18.nullpointerexception.takeabook.util;

import java.util.concurrent.atomic.AtomicInteger;

public class MyAtomicCounter{
    private OnCounterChangeListener listener;
    private AtomicInteger atomicInteger;

    public MyAtomicCounter(int initialValue){
        atomicInteger = new AtomicInteger(initialValue);
    }

    public OnCounterChangeListener getListener() {
        return listener;
    }

    public void setListener(OnCounterChangeListener listener) {
        this.listener = listener;
    }

    public void decrement(){
        int value = atomicInteger.decrementAndGet();
        if(listener!=null){
            if(value ==0){
                listener.onCounterReachZero();
            }
        }
    }
    public void increment(){
        int value = atomicInteger.incrementAndGet();
        if(listener!=null){
            if(value ==0){
                listener.onCounterReachZero();
            }
        }
    }
    public void set(int value){
        atomicInteger.set(value);
        if(listener!=null){
            if(value ==0){
                listener.onCounterReachZero();
            }
        }
    }
}
