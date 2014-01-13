#!/bin/sh

function usage() {
    echo "Usage: $0 <input filename> <output line count>"
    exit 0
}

if [ -z "$1" ] || [ ! -f "$1" ]; then
    echo "invalid input file"
    usage
fi

if [ -z "$2" ]; then
    usage
fi

input_file=$1
output_lc=$2
output_file=$1_$2
input_lc=$(wc -l ${input_file} | cut -d ' ' -f1)

if [ ${input_lc} -le ${output_lc} ]; then 
    echo "no need to do anything"
    exit 0
fi

rm -f $output_file
if [ ! -f RandPick.class ]; then
    echo "javac RandPick.java"
    javac RandPick.java
fi
for index in $(java -cp . RandPick ${input_lc} ${output_lc}); do
    echo $(head -n ${index} ${input_file} | tail -n 1) >> ${output_file}
done
