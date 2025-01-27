import csv
import sys
from collections import defaultdict
import os

def average_per_year(input_file):
    # Dictionary to store total and count per year
    year_data = defaultdict(lambda: {"total": 0, "count": 0})
    
    # Read input file and accumulate totals and counts per year
    with open(input_file, 'r') as f:
        reader = csv.reader(f, delimiter=' ')
        for row in reader:
            if len(row) < 2:
                continue  # Skip lines with insufficient data
            try:
                year = int(row[0])
                value = float(row[1])
                year_data[year]["total"] += value
                year_data[year]["count"] += 1
            except ValueError:
                print(f"Skipping invalid row: {row}")
    
    # Prepare output file name
    base, ext = os.path.splitext(input_file)
    output_file = f"{base}_avg{ext}"
    
    # Write the average per year to the output file
    with open(output_file, 'w') as f:
        writer = csv.writer(f, delimiter=' ')
        for year in sorted(year_data.keys()):
            total = year_data[year]["total"]
            count = year_data[year]["count"]
            average = total / count if count else 0
            writer.writerow([year, average])
    
    print(f"Output written to {output_file}")

# Check if file name is provided
if len(sys.argv) != 2:
    print("Usage: python script.py <input_file>")
    sys.exit(1)

# Call function with command-line argument
input_file = sys.argv[1]
average_per_year(input_file)

