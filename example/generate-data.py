#!/usr/bin/env python3
"""
generate-data.py — generates a fixed-width data file for the Phase 4 benchmark.

Usage:
    python generate-data.py [rows] [output]

    rows    number of records to generate (default: 1_000_000)
    output  output file path              (default: data/products.txt)

Record layout (80 chars per line):
    Offset  Length  Field
     0       8      id          (zero-padded integer)
     8      12      sku         (left-padded with spaces)
    20      20      name        (left-padded with spaces)
    40       8      price       (zero-padded cents, e.g. 00009999 = $99.99)
    48       6      quantity    (zero-padded)
    54       2      category    (EL, CL, FD, SP, HM)
    56       1      active      (Y/N)
    57       8      createdDate (yyyyMMdd)
    65       6      warehouseId (WH0001..WH0010)
    71       9      barcode     (9-digit zero-padded)
"""

import sys
import os
import random
import string
from datetime import date, timedelta

ROWS    = int(sys.argv[1]) if len(sys.argv) > 1 else 1_000_000
OUTPUT  = sys.argv[2]      if len(sys.argv) > 2 else "data/products.txt"

CATEGORIES  = ["EL", "CL", "FD", "SP", "HM"]
WAREHOUSES  = [f"WH{i:04d}" for i in range(1, 11)]
BASE_DATE   = date(2020, 1, 1)
DATE_RANGE  = (date.today() - BASE_DATE).days

def rand_str(length, chars=string.ascii_uppercase + string.digits):
    return "".join(random.choices(chars, k=length))

def make_record(i: int) -> str:
    id_         = str(i).zfill(8)
    sku         = rand_str(8).ljust(12)[:12]
    name        = rand_str(16).ljust(20)[:20]
    price       = str(random.randint(100, 99999)).zfill(8)
    quantity    = str(random.randint(0, 9999)).zfill(6)
    category    = random.choice(CATEGORIES)
    active      = random.choice(["Y", "N"])
    created     = (BASE_DATE + timedelta(days=random.randint(0, DATE_RANGE))).strftime("%Y%m%d")
    warehouse   = random.choice(WAREHOUSES)
    barcode     = str(random.randint(0, 999999999)).zfill(9)

    record = id_ + sku + name + price + quantity + category + active + created + warehouse + barcode
    assert len(record) == 80, f"Record length {len(record)} != 80 at row {i}"
    return record

os.makedirs(os.path.dirname(OUTPUT) if os.path.dirname(OUTPUT) else ".", exist_ok=True)

print(f"Generating {ROWS:,} records → {OUTPUT} ...")
with open(OUTPUT, "w", encoding="utf-8") as f:
    for i in range(1, ROWS + 1):
        f.write(make_record(i) + "\n")
        if i % 100_000 == 0:
            print(f"  {i:,} / {ROWS:,}")

print(f"Done. File size: {os.path.getsize(OUTPUT) / 1_048_576:.1f} MB")
