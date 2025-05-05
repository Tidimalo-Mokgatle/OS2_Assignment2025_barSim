#!/bin/bash
for i in {0..10}
do
make run ARGS="80 0 2 133 $i" > waste.txt
make run ARGS="80 0 2 133 $i" > waste.txt
make run ARGS="80 0 2 133 $i" > waste.txt
echo "."
make run ARGS="80 1 2 133 $i" > waste.txt
make run ARGS="80 1 2 133 $i" > waste.txt
make run ARGS="80 1 2 133 $i" > waste.txt
echo ".."
make run ARGS="80 2 2 133 $i" > waste.txt
make run ARGS="80 2 2 133 $i" > waste.txt
make run ARGS="80 2 2 133 $i" > waste.txt
echo "done with $i"
done 
rm waste.txt
