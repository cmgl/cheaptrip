#!/bin/sh
wget -O "vehicles.csv.zip" "https://www.fueleconomy.gov/feg/epadata/vehicles.csv.zip"
mkdir -p public_html
unzip -o "vehicles.csv.zip" -d public_html
rm "vehicles.csv.zip"
