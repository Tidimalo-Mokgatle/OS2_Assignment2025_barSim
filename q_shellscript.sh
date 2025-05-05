#!/bin/bash
for i in {1..210}
do
make run ARGS="30 2 3 $i 5" > waste.txt
echo "."
make run ARGS="30 2 3 $i 6" > waste.txt
echo ".."
make run ARGS="30 2 3 $i 7" > waste.txt
echo "..."
make run ARGS="30 2 3 $i 8" > waste.txt
echo "...."
make run ARGS="30 2 3 $i 9" > waste.txt
echo "done with $i"
done 
rm waste.txt
