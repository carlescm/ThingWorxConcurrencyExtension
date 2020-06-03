package com.wup;

import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceResult;

public class wupConcurrencySC  {

    @ThingworxServiceDefinition(
            name = "GetTotalActiveLocks_wupMutexTS", 
            description = "Returns the total active locks in the whole ThingWorx running system.", 
            category = "WUP", 
            isAllowOverride = false, 
            aspects = {"isAsync:false" }
            )
    @ThingworxServiceResult(name = "result", description = "The total ammount.", baseType = "LONG", aspects = {})
    public static long GetTotalActiveLocks_wupMutexTS(
            org.mozilla.javascript.Context cx,
            org.mozilla.javascript.Scriptable thisObj,
             Object[] args,
             org.mozilla.javascript.Function funObj) {
            return wupMutexTS.getTotalActiveLocks();
    }

    @ThingworxServiceDefinition(
        name = "GetTotalActiveWaiting_wupMutexTS", 
        description = "Returns the total active threads which are waiting on a lock in the whole ThingWorx running system.", 
        category = "WUP", 
        isAllowOverride = false, 
        aspects = {"isAsync:false" }
        )
    @ThingworxServiceResult(name = "result", description = "The total ammount.", baseType = "LONG", aspects = {})
    public static long GetTotalActiveWaiting_wupMutexTS(
        org.mozilla.javascript.Context cx,
        org.mozilla.javascript.Scriptable thisObj,
         Object[] args,
         org.mozilla.javascript.Function funObj) {
            return wupMutexTS.getTotalActiveWaiting();
    }
    
    @ThingworxServiceDefinition(
        name = "GetTotalThingsLocksUsage_wupMutexTS", 
        description = "Returns the total number of mutex created on Things (ReentranLocks), in the whole ThingWorx running system since last start.", 
        category = "WUP", 
        isAllowOverride = false, 
        aspects = {"isAsync:false" }
        )
    @ThingworxServiceResult(name = "result", description = "The total ammount.", baseType = "LONG", aspects = {})
    public static long GetTotalThingsLocksUsage_wupMutexTS(
        org.mozilla.javascript.Context cx,
        org.mozilla.javascript.Scriptable thisObj,
         Object[] args,
         org.mozilla.javascript.Function funObj) {
            return wupMutexTS.getTotalThingsLocksUsage();
    }

}
