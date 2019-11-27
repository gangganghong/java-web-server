#! /bin/bash

find /Users/cg/data/code/wheel/java/demo -name "log*" | xargs rm -vf
find /Users/cg/data/code/wheel/java/demo -name "tmp*" | xargs rm -vf 
find /Users/cg/data/code/wheel/java/demo -name "*.class" | xargs rm -vf
rm -rvf /Users/cg/data/code/wheel/java/demo/out
