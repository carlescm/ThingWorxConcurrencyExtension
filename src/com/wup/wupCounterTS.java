package com.wup;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.things.Thing;
import com.thingworx.types.primitives.LongPrimitive;

@ThingworxPropertyDefinitions(properties = {
    @ThingworxPropertyDefinition(name= wupCounterTS.PROPERTY_COUNTER, description="Counter atomic 'autoincrement' property. Don't update the value, at next increase will get previous value +1.", baseType="LONG", aspects={"isPersistent:true","defaultValue:0"})
})

public class wupCounterTS extends wupBaseThingShape {

    private static final long serialVersionUID = 1L;

    // -- This will create zombie counters if Things are deleted,
    // -- on server restart they will be freed, we may need to
    // -- implement a garbage collector... But deleting things it's
    // -- not a normal behaviour on ThingWorx.
    private static final ConcurrentHashMap<String, AtomicLong> _instanceCounter = new ConcurrentHashMap<>();

    public static final String PROPERTY_COUNTER = "counter_wupCounterTS";

    public static int getTotalThingsCounterUsage() {
        return wupCounterTS._instanceCounter.size();
     }

    private AtomicLong getInstanceCounter() throws Exception {
        final Thing me =  this.getMe();
        final String meName = me.getName();
        AtomicLong counter = wupCounterTS._instanceCounter.get(meName);
        if (counter == null) {
            synchronized(me) {
                long currentValue =  (long)me.getPropertyValue(wupCounterTS.PROPERTY_COUNTER).getValue();
                counter = wupCounterTS._instanceCounter.computeIfAbsent(meName, k -> new AtomicLong(currentValue));
            }
        }
        return counter;
    }

    @ThingworxServiceDefinition(
            name = "Increase_wupCounterTS", 
            description = "Increase an return counter value.", 
            category = "WUP", 
            isAllowOverride = false, 
            aspects = {"isAsync:false" }
            )
    @ThingworxServiceResult(name = "result", description = "The increased value", baseType = "LONG", aspects = {})
    public long Increase_wupCounterTS() throws Exception {
        final AtomicLong meCounter = this.getInstanceCounter();
        if (meCounter != null) {
            long newValue;
            while(true) {
                long existingValue = meCounter.get();
                newValue = existingValue + 1;
                if(meCounter.compareAndSet(existingValue, newValue)) {
                    break;
                }
            }
            // -- Let's set current property value.
            final Thing me =  this.getMe();
            synchronized(me) {
                long currentValue =  (long)me.getPropertyValue(wupCounterTS.PROPERTY_COUNTER).getValue();
                if (newValue>currentValue) {
                    // -- We can have various consurrent value increases and desordered, hence we will only write if new
                    // -- value it's bigger than previous one
                    me.setPropertyValue(wupCounterTS.PROPERTY_COUNTER,new LongPrimitive(newValue));
                }
            }
            return newValue;
        } 

        throw new Exception("Increase_wupCounterTS/Cannot get instance Counter");
    }

    @ThingworxServiceDefinition(
        name = "Set_wupCounterTS", 
        description = "Set/Reset counter value.", 
        category = "WUP", 
        isAllowOverride = false, 
        aspects = {"isAsync:false" }
        )
    public void Set_wupCounterTS(
        @ThingworxServiceParameter (name = "value", 
                    description = "Value to set the counter, usually used to reset to 0 or alike.", 
                    baseType = "LONG", 
                    aspects={"isRequired:false",
                            "defaultValue:0"
            }) final Long value) throws Exception {
        final AtomicLong meCounter = this.getInstanceCounter();
        if (meCounter != null) {
             // -- Let's set current property value.
             final Thing me =  this.getMe();
             synchronized(me) {
                meCounter.set((long)value);
                me.setPropertyValue(wupCounterTS.PROPERTY_COUNTER,new LongPrimitive(value));
            }
        } else {
            throw new Exception("Set_wupCounterTS/Cannot get instance Counter");
        }
    }

}
