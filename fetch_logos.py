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

SEARCH_URL = "https://ajax.googleapis.com/ajax/services/search/images"

OUTPUT_DIR = "images/team_logos"

LOGOS = {
    u"AGH University of Science and Technology": None,
    u"Adam Mickiewicz University": None,
    u"Budapest University of Technology and Economics": None,
    u"Charles University in Prague": None,
    u"Comenius University": None,
    u"Czech Technical University in Prague": None,
    u"Eötvös Loránd University": None,
    u"Jagiellonian University in Krakow": "http://www.meetthespace.org/wp-content/blogs.dir/1/files/logonew/logo_uj_labeled.png",
    u"Masaryk University": None,
    u"Nicolaus Copernicus University": None,
    u"Pavol Jozef Šafárik University in Košice": "http://hsci2013.info/sitefiles/image/photogallery/201210/logo-upjs-cb.jpg",
    u"Poznan University of Technology": "http://www.cs.put.poznan.pl/jbrzezinski/data/uploads/logopp-1.png",
    u"Slovak University of Technology in Bratislava": None,
    u"University of Applied Sciences Technikum Wien": "http://www.technikum-wien.at/sites/default/files/logo-300x160.png",
    u"University of Debrecen": None,
    u"University of Dubrovnik": None,
    u"University of Ljubljana": "http://www.asef.org/images/stories/partners/images/logo_universityofljubljana.jpg",
    u"University of Maribor": None,
    u"University of Osijek": None,
    u"University of Primorska": None,
    u"University of Szeged": None,
    u"University of Warsaw": "http://www.polmun.waw.pl/img/partners/UV.svg",
    u"University of West Bohemia in Pilsen": "http://www.eunis.cz/res/360/220?img=/_files/portfolio-images/13204048_zcu.png",
    u"University of Wroclaw": None,
    u"University of Zagreb": None,
    u"University of Žilina": None,
    u"VŠB - Technical University of Ostrava": None,
}


def fetch_and_save(team, url):
    logging.info("Fetching %s", url)
    r = requests.get(url)
    assert r.status_code == 200
    chunk_size = 1 << 20
    temp_file = os.path.join(OUTPUT_DIR, 'temp_xxx')
    png_file = os.path.join(OUTPUT_DIR, team + '.png')
    with open(temp_file, 'wb') as fd:
        for chunk in r.iter_content(chunk_size):
            fd.write(chunk)
    subprocess.check_call(['/usr/bin/convert',
                           '-background', 'white',
                           '-flatten',
                           '-resize', '300',
                           temp_file, png_file])


def first_google_hit(team):
    logging.info("Searching for team %s", team)
    r = requests.get(SEARCH_URL, params={'v': '1.0', 'q': team + ' logo'})
    j = json.loads(r.content)
    return j[u'responseData'][u'results'][0][u'unescapedUrl']


def fetch_and_save_all(teams):
    for team, url in teams.items():
        if url is None:
            url = first_google_hit(team)
        fetch_and_save(team, url)


if __name__ == "__main__":
    fetch_and_save_all(LOGOS)
