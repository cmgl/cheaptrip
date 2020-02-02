#!/bin/sh

# Portions of this script use the following open source software:
# csvquote, which is licensed under MIT license. A copy of this license can be obtained at
# https://opensource.org/licenses/MIT. The source code for this software is available at
# https://github.com/dbro/csvquote.

mkdir -p public_html
./csvq public_html/vehicles.csv | cut -d, -f1-4,6-15,17-30,32-34,36-46,49-57,59-63,65-83 --complement | ./csvq -u > temp.csv
sed -i '1d' temp.csv
./csvq temp.csv | sort -k6 -k3 -k4 -k2 -k5 -k1 -t, | ./csvq -u > vehicles_clean.csv
sed -i 1i"cityCo,combinedCo,fuelType,highwayCo,brand,model,transmission,year" vehicles_clean.csv
rm temp.csv
mv vehicles_clean.csv public_html/
