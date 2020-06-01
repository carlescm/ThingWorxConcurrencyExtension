# ThingWorx Concurrency Extension

The aim of this extension it's to provide the Swiss Tool for Concurrency in ThingWorx.

## Contents

- [How It Works](#how-it-works)
- [Compatibility](#compatibility)
- [Installation](#installation)
- [Usage](#usage)
    - [Mutex](#mutex)
    - [Counter](#counter)
    - [Concurrency Script Functions](#concurrency-script-functions)
- [Build](#build)
- [Acknowledgments](#acknowledgments)
- [Author](#author)


## How It Works

It publishes standard Java concurrency features in order to be used easily on ThingWorx Server Side Javascript. Also, it may try to solve
typical concurrency problems like doing an autoincrement counter.

## Compatibility

ThingWorx 7.3 and above. It's set to minimum ThingWorx 6.5 and built with ThingWorx 6.5 SDK but I didn't tested with it.

## Installation

Import the extension (ConcurrencyExtension.zip) with ThingWorx Composer Import Extension feature.

## Usage

### Mutex 

We implemented a mutex ThingShape with Java ReentrantLook with fair execution enabled which allows to synchronize and block thread execution.
Blocking it's done at Thing's level, et means each Thing which implements the wupMutexTS ThingShape has it's own ReentrantLook.

#### Mutex Usage Samples

Just add the wupMutexTS to the Thing or ThingTemplate to whom you want to add mutex blocking features.

In order to lock a piece of code in Javascript, and ensure that only one thread its entering on it at a time:

```javascript
me.Lock_wupMutexTS();
try {
    // -- whatever code that needs to be mutex
} finally { 
    me.Unlock_wupMutexTS();
}
```
You can also tryLock a piece of code, in order to allow one thread and only one and discard the others.
For instance it may be interesting if you have a timer which triggers once in a while and you don't want that two
consecutive triggers are executed at the same time:

```javascript
if (me.TryLock_wupMutexTS()===true) {
    try {
        // -- whatever code that needs to be mutex
    } finally { 
        me.Unlock_wupMutexTS();
    }
} else {
    // -- The lock was already got and previous code its skipped
}
```

You can create more than one mutex per thing, all wupMutexTS services has an optional "id" parameter, which allows to create a more quirurgic mutex.
Each different passed "id" will create its own ReentrantLook. Sample with previous code but with a specific lock for one specific timer.

```javascript
if (me.TryLock_wupMutexTS({ id: "timer1" })===true) {
    try {
        // -- whatever code that needs to be mutex
    } finally { 
        me.Unlock_wupMutexTS({ id: "timer1" });
    }
} else {
    // -- The lock was already got and previous code it's skipped
}
```

### Counter 

A thread safe "autoincrement" ThingShape. It provides a "counter" property and the corresponding services in order to increase (one by one) it's value.

#### Counter Usage Samples

To increase the counter value, no worries about having any amount of threads incrementing the property value, all will get it's own and unique value:

```javascript
var newCounterValue = me.Increase_wupCounterTS();
```

You can reset or reset counter value with Set_wupCounterTS method:

```javascript
me.Set_wupCounterTS({ value: 0 });
```

The counter it's stored and update on a persistent property named: counter_wupCounterTS

### Concurrency Script Functions

List of script helper functions related with this concurrency extension. This services should go on a subsystem like entity, but subsystems on ThingWorx can't be created through extensions :(

#### GetTotalActiveLocks_wupMutexTS

Returns the total active locks, in the whole ThingWorx running system.

#### GetTotalActiveWaiting_wupMutexTS

Returns the total active threads which are waiting on a lock, in the whole ThingWorx running system.

#### GetTotalThingsLocksUsage_wupMutexTS

Returns the total number of mutex created on Things (ReentranLocks), in the whole ThingWorx running system since last start.

## Build

If you need to build it, it's built with ant and java 8 on a MacOS, the scripts are on the repository. Version change it's done by hand and should be automated.

## Acknowledgments

I've started from the [code](https://community.ptc.com/t5/ThingWorx-Developers/Concurrency-Synchronisation-ConcurrentModificationException/m-p/624921) posted by [@antondorf](https://community.ptc.com/t5/user/viewprofilepage/user-id/290654) on the ThingWorx Developer Community.


## Author

[Carles Coll Madrenas](https://linkedin.com/in/carlescoll)
