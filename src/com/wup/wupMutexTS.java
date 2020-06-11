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

     private static ReentrantLock getMutexById(String id) throws Exception {
        ReentrantLock meMtx = wupMutexTS._instanceMtx.get(id);
        if (meMtx == null) {
            meMtx = wupMutexTS._instanceMtx.computeIfAbsent(id, k -> new ReentrantLock(true));
        }
        return meMtx;
     }

     public static void lock(String id) throws Exception {
        final ReentrantLock mutex = wupMutexTS.getMutexById(id);
        if (mutex != null) {
            wupMutexTS.incrementWaiting();
            mutex.lock();
            // -- we must ensure that the lock it's returned, otherwise we must unlock here.
            try {
                wupMutexTS.decrementWaiting();
                wupMutexTS.incrementLocks();
            } catch(Exception e) {
                mutex.unlock();
                throw new Exception("Lock_wupMutexTS/Failed to to additional steps, waiting counter maybe corrupted.");
            }
        } else {
            throw new Exception("Lock_wupMutexTS/Cannot get instance Mutex");
        }
     }

     public static Boolean tryLock(String id,Long timeOut) throws Exception {
        final ReentrantLock mutex = wupMutexTS.getMutexById(id);
        if (mutex != null) {
            final Boolean result;
            Boolean incremented = false;
            if (((long)timeOut)<0) {
              result = mutex.tryLock();
            } else {
              incremented = true;
              wupMutexTS.incrementWaiting();
              result = mutex.tryLock((long) timeOut, TimeUnit.MILLISECONDS);      
            }

            if (result==true) {
                // -- we must ensure that the lock it's returned, otherwise we must unlock here.
                try{
                  if (incremented==true) wupMutexTS.decrementWaiting();  
                   wupMutexTS.incrementLocks();
                } catch(Exception e) {
                    mutex.unlock();
                    throw new Exception("TryLock_wupMutexTS/Failed to do additional steps, waiting counter maybe corrupted.");
                }
            }
            return result;
        } else {
            throw new Exception("TryLock_wupMutexTS/Cannot get instance Mutex");
        }
     }
     public static void unlock(String id) throws Exception {
        final ReentrantLock mutex = wupMutexTS.getMutexById(id);
        if (mutex != null) {
            mutex.unlock();
            wupMutexTS.decrementLocks();
        } else {
            throw new Exception("Unlock_wupMutexTS/Cannot get instance Mutex");
        }
     }

     public static Boolean isLocked(String id) throws Exception {
        final ReentrantLock mutex = wupMutexTS.getMutexById(id);
        if (mutex != null) {
            return mutex.isLocked();
        }
        return false;
     }

     private String getInstanceMutexId(String id/*optional*/) throws Exception {
        String mutexId =  this.getMeName();
        if (id!=null) {
            if (!id.equals("")) mutexId = mutexId+"/"+id;
        }
        return mutexId;
    }
 

    @ThingworxServiceDefinition(
            name = "Lock_wupMutexTS", 
            description = "Get a exclusive Lock for this thing. Recomended usage:\n "+
            " var meName = me.name;\n"+
            " me.Lock_wupMutexTS(); \n"+
            " try {\n"+
            "   // -- whatever code that needs to be mutex \n"+
            " } finally { \n"+
            "    // -- The following line it's thing restarts almost prone. \n"+
            "   for(var i=0;i<120;i++) { try { Things[meName].Unlock_wupMutexTS(); break; } catch(err) { pause(1000); } }  \n"+
            " }", 
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
        wupMutexTS.lock(this.getInstanceMutexId(id));
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
        return wupMutexTS.tryLock(this.getInstanceMutexId(id), timeOut);
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
        wupMutexTS.unlock(this.getInstanceMutexId(id));
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
        return wupMutexTS.isLocked(this.getInstanceMutexId(id));
    }

}
