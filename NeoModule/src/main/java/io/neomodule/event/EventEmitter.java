package io.neomodule.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kiva
 */

public class EventEmitter {
    public interface EventCallback {
        void onTrigger(Object... args);
    }

    private Map<Object, EventCallback> EVENTS = new ConcurrentHashMap<>();

    public final void on(Object trigger, EventCallback eventCallback) {
        if (!EVENTS.containsKey(trigger)) {
            EVENTS.put(trigger, eventCallback);
        }
    }

    public final void emit(Object trigger, Object... args) {
        EventCallback eventCallback = EVENTS.get(trigger);
        if (eventCallback != null) {
            eventCallback.onTrigger(args);
        }
    }
}
