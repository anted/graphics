#!/usr/bin/env python2
# -*- coding: utf-8 -*-

import json
import logging
import os
import requests
import subprocess

logging.basicConfig(
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    level=logging.INFO,
)

OUTPUT_DIR = "images/flags"

FLAG_URL = "https://flagspot.net/images/{}/{}.gif"

COUNTRIES = {
    "Austria": "at", 
    "Croatia": "hr",
    "Czech Republic": "cz",
    "Hungary": "hu",
    "Poland": "pl",
    "Slovakia": "sk",
    "Slovenia": "si",
}


def fetch_and_save(country, url):
    logging.info("Fetching %s", url)
    r = requests.get(url)
    assert r.status_code == 200
    chunk_size = 1 << 20
    temp_file = os.path.join(OUTPUT_DIR, 'temp_xxx')
    png_file = os.path.join(OUTPUT_DIR, country.lower() + '.png')
    with open(temp_file, 'wb') as fd:
        for chunk in r.iter_content(chunk_size):
            fd.write(chunk)
    fd.close()
    subprocess.check_call(['/usr/bin/convert',
                           '-resize', '300x300',
                           temp_file,
                           '-background', 'none',
                           '-gravity', 'center',
                           '-extent', '300x300',
                           png_file])


def fetch_and_save_all(countries):
    for country, code in countries.items():
        url = FLAG_URL.format(code[0], code)
        fetch_and_save(country, url)


if __name__ == "__main__":
    fetch_and_save_all(COUNTRIES)
