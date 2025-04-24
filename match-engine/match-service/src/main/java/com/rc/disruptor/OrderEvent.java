package com.rc.disruptor;

import lombok.Data;
import lombok.Setter;

import java.io.Serializable;

@Data

public class OrderEvent implements Serializable {

    private static final long serialVersionUID = 5516075349620653480L;

    /**
     * 时间戳
     */
    private final long timestamp;

    /**
     * 事件携带的对象
     */
    protected transient Object source;


    public OrderEvent(Object source) {
        timestamp = System.currentTimeMillis();
        this.source = source;
    }

    public OrderEvent() {
        timestamp = System.currentTimeMillis();
    }

    /**
     * Clearing Objects From the Ring Buffer
     */
    public void clear() {
        this.source = null;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Object getSource() {
        return source;
    }

}

