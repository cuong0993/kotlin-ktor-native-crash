#!/bin/bash

BUILD_REPORTS="${BUILD_REPORTS:=./build/reports/gatling}"
GATLING_RESULTS="${GATLING_RESULTS:=./gatling-charts/results/}"

# get all logs
mkdir -p "$BUILD_REPORTS"

i=1
find "$BUILD_REPORTS" -name '*.log' -print0 | while IFS= read -r -d $'\0' file;
do
    BUILD_REPORTS_FILE="$BUILD_REPORTS/$i-simulation.log"
    echo "Moving: $BUILD_REPORTS_FILE"
    cp -a "$file" "$BUILD_REPORTS_FILE"
    ((i=i+1))
done

# load gatling charts plugin
if [ ! -d gatling-charts ]; then
    wget https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/3.9.3/gatling-charts-highcharts-bundle-3.9.3-bundle.zip
    unzip gatling-charts-highcharts-bundle-3.9.3-bundle.zip
    rm -f gatling-charts-highcharts-bundle-3.9.3-bundle.zip
    mv -f gatling-charts-highcharts-bundle-3.9.3 gatling-charts
fi

# merge all logs
cp -a "$BUILD_REPORTS" "$GATLING_RESULTS"
./gatling-charts/bin/gatling.sh -ro "./"
