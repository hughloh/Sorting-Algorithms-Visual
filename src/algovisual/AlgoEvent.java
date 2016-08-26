/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algovisual;

import javafx.event.Event;
import javafx.event.EventType;

/**
 *
 * @author meng
 */
public class AlgoEvent extends Event {
    
    public static final EventType<AlgoEvent> ALGO_EVENT = new EventType<>(EventType.ROOT, "Algo Event");
    public static final EventType<AlgoEvent> COMPARE_EVENT = new EventType<>(ALGO_EVENT, "Compare Event");
    public static final EventType<AlgoEvent> SWAP_EVENT = new EventType<>(ALGO_EVENT, "Swap Event");
    public static final EventType<AlgoEvent> COPY_EVENT = new EventType<>(ALGO_EVENT, "Copy Event");
    public static final EventType<AlgoEvent> FINISHED_EVENT = new EventType<>(ALGO_EVENT, "Finished Event");
    
    private final int[] indices;
    
    public AlgoEvent(EventType<? extends AlgoEvent> eventType, int... indices) {
        super(eventType);
        this.indices = indices;
    }
    
    public int getIndex1() { return indices[0]; }
    public int getIndex2() { return indices[1]; }
    
    public int[] getIndices() { return indices; }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getEventType().getName()).append(" - ");
        for (int i : indices) sb.append(i).append(" ");
        return sb.toString();
    }
}