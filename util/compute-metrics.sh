#!/bin/bash

# Description:
# This script uses 'cloc' to count the number of lines of Java code for each .java file
# in a specified directory and all its subdirectories (recursive search).
# It then outputs:
#   - A list of these files ordered from the longest to the shortest
#   - Total lines of code
#   - Average lines of code per file

# Function to display usage information
usage() {
  echo "Usage: $0 [directory]"
  echo "  If no directory is specified, the current directory is used."
  exit 1
}

# Check if help is requested
if [[ "$1" == "-h" || "$1" == "--help" ]]; then
  usage
fi

# Determine the target directory
if [ $# -gt 1 ]; then
  echo "Error: Too many arguments."
  usage
elif [ $# -eq 1 ]; then
  target_dir="$1"
else
  target_dir="."
fi

# Verify that the target directory exists and is a directory
if [ ! -d "$target_dir" ]; then
  echo "Error: '$target_dir' is not a valid directory."
  exit 1
fi

# Check if 'cloc' is installed
if ! command -v cloc >/dev/null 2>&1; then
  echo "Error: 'cloc' is not installed. Please install it and try again."
  exit 1
fi

# Initialize total lines and file count
total=0
count=0

# Temporary file to store filenames and line counts
temp_file=$(mktemp)

# Function to clean up temporary file on exit
cleanup() {
  rm -f "$temp_file"
}
trap cleanup EXIT

# Use 'cloc' to get the line counts per file
# --quiet suppresses non-essential output
# --csv ensures output is in CSV format
# --include-lang=Java filters for Java files only
cloc_output=$(cloc --by-file --include-lang=Java --csv "$target_dir" 2>/dev/null)

# Process 'cloc' output
# The CSV output has fields: language,filename,blank,comment,code
# We will extract filename and code lines

# Skip the header line and process each Java file
total=0
echo "$cloc_output" | tail -n +2 | while IFS=',' read -r language filename blank comment code; do
  # Remove quotes from filename if any
  filename=${filename//\"/}

  # if filename is "package-info.java", skip it
  # as it is not a regular Java source file
  if [[ "$filename" == *"/package-info.java" ]]; then
    continue
  fi

  # Remove leading/trailing whitespace from code
  code=$(echo "$code" | xargs)

  # Ensure that code is a number
  if ! [[ "$code" =~ ^[0-9]+$ ]]; then
    continue
  fi

  # Add to total lines
  total=$((total + code))
  echo "$total"

  # Increment file count
  count=$((count + 1))
  echo "$count"

  # Save the code line count and filename to the temporary file
  echo "$code $filename" >>"$temp_file"
done

# Sort the temporary file in descending order based on code line count
sorted=$(sort -nr "$temp_file")

# Output the sorted list
echo "List of Java files in '$target_dir' (recursively) ordered from longest to shortest:"
echo "-----------------------------------------------------------------------"
printf "%10s  %s\n" "Lines" "Filename"
echo "-----------------------------------------------------------------------"
echo "$sorted" | while read -r line_count filename; do
  printf "%10s  %s\n" "$line_count" "$filename"
done
echo "-----------------------------------------------------------------------"

# Output total lines of code
echo "Total lines of code: $total"

# Calculate and output average lines of code per file
if [ "$count" -gt 0 ]; then
  # Calculate average with decimal precision using bc or awk
  if command -v bc >/dev/null 2>&1; then
    average=$(echo "scale=2; $total / $count" | bc)
  else
    # If bc is not available, use awk as an alternative
    average=$(awk "BEGIN {printf \"%.2f\", $total/$count}")
  fi
  echo "Average lines of code per file: $average"
else
  echo "Average lines of code per file: N/A (no files)"
fi
