#!/bin/bash

for file in test-suite/PrintTest/data/*.le; do
    echo -e "\e[36m\e[1m>>>>> " $file " <<<<<\e[0m"

    java -cp out/production/Template analyzer.Main $file

    echo
done
