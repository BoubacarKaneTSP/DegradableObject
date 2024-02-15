import os
import sys

numSet = 0
numQueue = 0
numMap = 0
numLong = 0

methodsSet = set()
methodsQueue = set()
methodsMap = set()
methodsLong = set()

nbMethodsSet = 0
nbMethodsQueue = 0
nbMethodsMap = 0
nbMethodsLong = 0

dictSet = {}
dictQueue = {}
dictMap = {}
dictLong = {}

pDictSet = {}
pDictQueue = {}
pDictMap = {}
pDictLong = {}


with open("result.txt") as file:
    for line in file:
        words = line.split()
        if words[1] == "ConcurrentSkipListSet":
            numSet+=1
            methodsSet.update(words[4:])

        if words[1] == "ConcurrentLinkedQueue":
            numQueue+=1
            methodsQueue.update(words[4:])

        if words[1] == "ConcurrentHashMap":
            numMap+=1
            methodsMap.update(words[4:])

        if words[1] == "AtomicLong":
            numLong+=1
            methodsLong.update(words[4:])

for m in methodsSet:
    dictSet[m] = 0
    # pDictSet = 0

for m in methodsQueue:
    dictQueue[m] = 0
    # pDictQueue = 0

for m in methodsMap:
    dictMap[m] = 0
    # pDictMap = 0

for m in methodsLong:
    dictLong[m] = 0
    # pDictLong = 0

with open("result.txt") as file:
    for line in file:
        words = line.split()
        if words[1] == "ConcurrentSkipListSet":
            for method in words[4:]:
                dictSet[method] += 1
                nbMethodsSet += 1

        if words[1] == "ConcurrentLinkedQueue":
            for method in words[4:]:
                dictQueue[method] += 1
                nbMethodsQueue += 1

        if words[1] == "ConcurrentHashMap":
            for method in words[4:]:
                dictMap[method] += 1
                nbMethodsMap += 1

        if words[1] == "AtomicLong":
            for method in words[4:]:
                dictLong[method] += 1
                nbMethodsLong += 1

for cle, val in dictSet.items():
    if round((val/nbMethodsSet)*100, 2) > 2 :
        pDictSet[cle] = round((val/nbMethodsSet)*100, 2)

for cle, val in dictMap.items():
    if round((val/nbMethodsMap)*100, 2) > 2:
        pDictMap[cle] = round((val/nbMethodsMap)*100, 2)

for cle, val in dictQueue.items():
    if round((val/nbMethodsQueue)*100, 2) > 2:
        pDictQueue[cle] = round((val/nbMethodsQueue)*100, 2)

for cle, val in dictLong.items():
    if round((val/nbMethodsLong)*100, 2) > 2:
        pDictLong[cle] = round((val/nbMethodsLong)*100, 2)

print ("numSet :", numSet)
print ("numMap :", numMap)
print ("numLong :", numLong)
print ("numQueue :", numQueue)
print ()
print ("methods in set :", methodsSet)
print ("methods in map :", methodsMap)
print ("methods in queue :", methodsQueue)
print ("methods in long :", methodsLong)
print ()
print ("count methods for class Set :", dictSet)
print ("count methods for class Map :", dictMap)
print ("count methods for class Queue :", dictQueue)
print ("count methods for class Long :", dictLong)
print ()
print ("% methods for class Set :", pDictSet)
print ("% methods for class Map :", pDictMap)
print ("% methods for class Queue :", pDictQueue)
print ("% methods for class Long :", pDictLong)
print ()
print ("nb methods total Set :", nbMethodsSet)
print ("nb methods total Map :", nbMethodsMap)
print ("nb methods total Queue :", nbMethodsQueue)
print ("nb methods total Long :", nbMethodsLong)