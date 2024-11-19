# Start the Pattern4J analysis
java -Xms32m -Xmx512m \
  -jar ./tools/pattern4.jar \
  -target "./minum/target/classes/com/renomad/minum" \
  -output ./out/scan.csv
