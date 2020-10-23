#!/bin/bash
./prepare.sh
./run.sh < src/test/resources/input.txt > src/test/resources/tmp.txt
diff src/test/resources/tmp.txt src/test/resources/output.txt
rm src/test/resources/tmp.txt
