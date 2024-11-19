#!/usr/bin/env bash

# Description:
# This script counts the number of lines of Java code for each .java file
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

# Find all .java files in the target directory and subdirectories
# Using find with -type f to ensure regular files
# Using -print0 to handle filenames with spaces or special characters
find "$target_dir" -type f -name "*.java" -print0 | while IFS= read -r -d '' file; do
  # Get the line count using wc, stripping leading/trailing whitespace
  lines=$(wc -l <"$file" | tr -d '[:space:]')

  # Add to total lines
  total=$((total + lines))

  # Increment file count
  count=$((count + 1))

  # Save the line count and relative filename to the temporary file
  # Using realpath --relative-to to get the relative path from target_dir
  relative_path=$(realpath --relative-to="$target_dir" "$file")
  echo "$lines $relative_path" >>"$temp_file"
done

# Check if any .java files were found
if [ $count -eq 0 ]; then
  echo "No Java files found in directory '$target_dir' and its subdirectories."
  exit 0
fi

# Sort the temporary file in descending order based on line count
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
if [ $count -gt 0 ]; then
  # Calculate average with decimal precision using bc
  average=$(echo "scale=2; $total / $count" | bc)
  echo "Average lines of code per file: $average"
else
  echo "Average lines of code per file: N/A (no files)"
fi
