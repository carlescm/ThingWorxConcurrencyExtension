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

   
    @ThingworxServiceDefinition(
            name = "Lock_wupMutexTS", 
            description = "Get a exclusive Lock for the given id, for instance a Thing Name. Recomended usage:\n "+
            " var meName = me.name;\n"+
            " Lock_wupMutexTS(meName); \n"+
            " try {\n"+
            "   // -- whatever code that needs to be mutex \n"+
            " } finally { \n"+
            "   Unlock_wupMutexTS(meName); \n"+
            "}", 
            category = "WUP", 
            isAllowOverride = false, 
            aspects = {"isAsync:false" }
            )
    @ThingworxServiceResult(name = "result", description = "", baseType = "NOTHING", aspects = {})
    public static void Lock_wupMutexTS(
        org.mozilla.javascript.Context cx,
        org.mozilla.javascript.Scriptable thisObj,
         Object[] args,
         org.mozilla.javascript.Function funObj) throws Exception {
          wupMutexTS.lock(args[0].toString());  
    }

    @ThingworxServiceDefinition(
        name = "TryLock_wupMutexTS", 
        description = "Get a exclusive Lock for this thing with or without a timout.",
        category = "WUP", 
        isAllowOverride = false, 
        aspects = {"isAsync:false" }
        )
    @ThingworxServiceResult(name = "result", description = "Returns true if the lock was acquired, false otherwise. If -1, does a tryLock without a timeout", baseType = "BOOLEAN", aspects = {})
    public static Boolean TryLock_wupMutexTS(
        org.mozilla.javascript.Context cx,
        org.mozilla.javascript.Scriptable thisObj,
        Object[] args,
        org.mozilla.javascript.Function funObj) throws Exception {
        Long timeOut = Long.valueOf(-1);
        if (args.length>1) {
            timeOut = Long.parseLong(args[1].toString());
        }
        return wupMutexTS.tryLock(args[0].toString(),timeOut);  
    }

    @ThingworxServiceDefinition(
            name = "Unlock_wupMutexTS", 
            description = "Unlock a exclusive Lock for the given id, for instance a Thing Name.",
            category = "WUP", 
            isAllowOverride = false, 
            aspects = {"isAsync:false" }
            )
    @ThingworxServiceResult(name = "result", description = "", baseType = "NOTHING", aspects = {})
    public static void Unlock_wupMutexTS(
        org.mozilla.javascript.Context cx,
        org.mozilla.javascript.Scriptable thisObj,
         Object[] args,
         org.mozilla.javascript.Function funObj) throws Exception {
          wupMutexTS.unlock(args[0].toString());  
    }

    @ThingworxServiceDefinition(
        name = "IsLocked_wupMutexTS", 
        description = "Check if current lock it's acquiered.",
        category = "WUP", 
        isAllowOverride = false, 
        aspects = {"isAsync:false" }
        )
    @ThingworxServiceResult(name = "result", description = "", baseType = "BOOLEAN", aspects = {})
    public static Boolean IsLocked_wupMutexTS(
        org.mozilla.javascript.Context cx,
        org.mozilla.javascript.Scriptable thisObj,
        Object[] args,
        org.mozilla.javascript.Function funObj) throws Exception {
        return wupMutexTS.isLocked(args[0].toString());  
    }


}
