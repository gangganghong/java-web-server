#! /bin/bash

find /Users/cg/data/code/wheel/java/demo/src -name "log*" | xargs rm -vf
find /Users/cg/data/code/wheel/java/demo/src -name "tmp*" | xargs rm -vf 
find /Users/cg/data/code/wheel/java/demo/src -name "*.class" | xargs rm -vf
rm -rvf /Users/cg/data/code/wheel/java/demo/src/out
