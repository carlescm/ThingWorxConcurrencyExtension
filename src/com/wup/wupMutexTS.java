package com.wup;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;

public class wupMutexTS extends wupBaseThingShape {

    private static final long serialVersionUID = 1L;

    // -- Only one Mutex instance for the whole system and thing, event if ThingsRestart
    // -- we don't kill mutex as maybe there's processes executing on the background.
    // -- This will create zombie mutex if Things are deleted,
    // -- on server restart they will be freed, we may need to
    // -- implement a garbage collector... But deleting things it's
    // -- not a normal behaviour on ThingWorx.
    private static final ConcurrentHashMap<String, ReentrantLock> _instanceMtx = new ConcurrentHashMap<>();

    private static AtomicInteger activeLocks = new AtomicInteger(0);
    private static AtomicInteger activeWaiting = new AtomicInteger(0);

    private static void incrementLocks() {
        while(true) {
            int existingValue = wupMutexTS.activeLocks.get();
            int newValue = existingValue + 1;
            if(wupMutexTS.activeLocks.compareAndSet(existingValue, newValue)) {
                return;
            }
        }
    }

    private static void decrementLocks() {
        while(true) {
            int existingValue = wupMutexTS.activeLocks.get();
            int newValue = existingValue - 1;
            if(wupMutexTS.activeLocks.compareAndSet(existingValue, newValue)) {
                return;
            }
        }
    }
    private static void incrementWaiting() {
        while(true) {
            int existingValue = wupMutexTS.activeWaiting.get();
            int newValue = existingValue + 1;
            if(wupMutexTS.activeWaiting.compareAndSet(existingValue, newValue)) {
                return;
            }
        }
    }

    private static void decrementWaiting() {
        while(true) {
            int existingValue = wupMutexTS.activeWaiting.get();
            int newValue = existingValue - 1;
            if(wupMutexTS.activeWaiting.compareAndSet(existingValue, newValue)) {
                return;
            }
        }
    }

    public static int getTotalActiveLocks() {
       return wupMutexTS.activeLocks.get();
    }
    public static int getTotalActiveWaiting() {
        return wupMutexTS.activeWaiting.get();
     }

    public static int getTotalThingsLocksUsage() {
        return wupMutexTS._instanceMtx.size();
     }


    private ReentrantLock getInstanceMutex(String id/*optional*/) throws Exception {
        String mutexId =  this.getMeName();
        if (id!=null) {
            if (!id.equals("")) mutexId = mutexId+"/"+id;
        }
        ReentrantLock meMtx = wupMutexTS._instanceMtx.get(mutexId);
        if (meMtx == null) {
            meMtx = wupMutexTS._instanceMtx.computeIfAbsent(mutexId, k -> new ReentrantLock(true));
        }
        return meMtx;
    }
 

    @ThingworxServiceDefinition(
            name = "Lock_wupMutexTS", 
            description = "Get a exclusive Lock for this thing. Recomended usage:\n "+
            " me.Lock_wupMutexTS(); \n"+
            " try {\n"+
            "   // -- whatever code that needs to be mutex \n"+
            " } finally { \n"+
            "   me.Unlock_wupMutexTS(); \n"+
            "}", 
            category = "WUP", 
            isAllowOverride = false, 
            aspects = {"isAsync:false" }
            )
    @ThingworxServiceResult(name = "result", description = "", baseType = "NOTHING", aspects = {})
    public void Lock_wupMutexTS(
        @ThingworxServiceParameter(name = "id", 
                    description = "Optional Mutex Name in order to have more than one Mutex for the same thing, for instance to be more quirurgic on the blocking condition.", 
                    baseType = "STRING", 
                    aspects={"isRequired:false"}
                    ) final String id
    ) throws Exception {
        final ReentrantLock meMtx = this.getInstanceMutex(id);
        if (meMtx != null) {
            wupMutexTS.incrementWaiting();
            meMtx.lock();
            // -- we must ensure that the lock it's returned, otherwise we must unlock here.
            try {
                wupMutexTS.decrementWaiting();
                wupMutexTS.incrementLocks();
            } catch(Exception e) {
                meMtx.unlock();
                throw new Exception("Lock_wupMutexTS/Failed to to additional steps, waiting counter maybe corrupted.");
            }
        } else {
            throw new Exception("Lock_wupMutexTS/Cannot get instance Mutex");
        }
    }

    @ThingworxServiceDefinition(name = "TryLock_wupMutexTS", description = "Get a exclusive Lock for this thing with or without a timout.", category = "WUP", isAllowOverride = false, aspects = {
            "isAsync:false" })
    @ThingworxServiceResult(name = "result", description = "Returns true if the lock was acquired, false otherwise. If -1, does a tryLock without a timeout", baseType = "BOOLEAN", aspects = {})
    public Boolean TryLock_wupMutexTS(
            @ThingworxServiceParameter(name = "id", 
            description = "Optional Mutex Name in order to have more than one Mutex for the same thing, for instance to be more quirurgic on the blocking condition.", 
            baseType = "STRING", 
            aspects={"isRequired:false"}
            ) final String id ,
            @ThingworxServiceParameter(name = "timeOut", 
                        description = "Timeout in milliseconds. Default = -1 if you don't want a timeout -> only one it's allowed others are discarded.", 
                        baseType = "LONG", 
                        aspects={"isRequired:false",
                                "defaultValue:-1"
                }) final Long timeOut
    ) throws Exception {
        final ReentrantLock meMtx = this.getInstanceMutex(id);
        if (meMtx != null) {
            final Boolean result;
            Boolean incremented = false;
            if (((long)timeOut)<0) {
              result = meMtx.tryLock();
            } else {
              incremented = true;
              wupMutexTS.incrementWaiting();
              result = meMtx.tryLock((long) timeOut, TimeUnit.MILLISECONDS);      
            }

            if (result==true) {
                // -- we must ensure that the lock it's returned, otherwise we must unlock here.
                try{
                  if (incremented==true) wupMutexTS.decrementWaiting();  
                   wupMutexTS.incrementLocks();
                } catch(Exception e) {
                    meMtx.unlock();
                    throw new Exception("TryLock_wupMutexTS/Failed to do additional steps, waiting counter maybe corrupted.");
                }
            }
            return result;
        } else {
            throw new Exception("TryLock_wupMutexTS/Cannot get instance Mutex");
        }
    }

    @ThingworxServiceDefinition(name = "Unlock_wupMutexTS", description = "Freeds the current lock for the thing.", category = "WUP", isAllowOverride = false, aspects = {
            "isAsync:false" })
    @ThingworxServiceResult(name = "result", description = "", baseType = "NOTHING", aspects = {})
    public void Unlock_wupMutexTS(
        @ThingworxServiceParameter(name = "id", 
        description = "Optional Mutex Name in order to have more than one Mutex for the same thing, for instance to be more quirurgic on the blocking condition.", 
        baseType = "STRING", 
        aspects={"isRequired:false"}
        ) final String id
    ) throws Exception {
        final ReentrantLock meMtx = this.getInstanceMutex(id);
        if (meMtx != null) {
            meMtx.unlock();
            wupMutexTS.decrementLocks();
        } else {
            throw new Exception("Unlock_wupMutexTS/Cannot get instance Mutex");
        }
    }

    @ThingworxServiceDefinition(name = "IsLocked_wupMutexTS", description = "Check if current lock it's acquiered.", category = "WUP", isAllowOverride = false, aspects = {
            "isAsync:false" })
    @ThingworxServiceResult(name = "result", description = "", baseType = "BOOLEAN", aspects = {})
    public Boolean IsLocked_wupMutexTS(
                @ThingworxServiceParameter(name = "id", 
                description = "Optional Mutex Name in order to have more than one Mutex for the same thing, for instance to be more quirurgic on the blocking condition.", 
                baseType = "STRING", 
                aspects={"isRequired:false"}
                ) final String id
    ) throws Exception {
        final ReentrantLock meMtx = this.getInstanceMutex(id);
        if (meMtx != null) {
            return meMtx.isLocked();
        }
        return false;
    }

}
