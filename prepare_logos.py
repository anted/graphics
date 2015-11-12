#!/usr/bin/env python2
# -*- coding: utf-8 -*-

import logging
import os
import sys
import subprocess
import xml.etree.ElementTree as ET

logging.basicConfig(
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    level=logging.INFO,
)

INPUT_DIR = "images/team_logos"
OUTPUT_DIR = "images/logos"
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
    u"Poznan University of Technology": "http://www.cmwm.put.poznan.pl/sites/default/files/PP_eng.jpg",
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


# <team>
#   <external-id>208</external-id>
#   <id>4</id>
#   <name>Slovak University of Technology in Bratislava team 1 </name>
#   <nationality>Croatia</nationality>
#   <region>Central Europe</region>
#   <university>UNIZG</university>
# </team>

def get_university(team_name):
    for uni in LOGOS:
        if team_name.startswith(uni):
            return uni
    assert False


def process_file(filename):
    tree = ET.parse(filename)
    root = tree.getroot()
    for team in root.findall('team'):
        id = team.find('id').text
        name = team.find('name').text
        univ_name = get_university(name)
        file_from = os.path.join(INPUT_DIR, univ_name + '.png')
        file_to = os.path.join(OUTPUT_DIR, id + '.png')
        logging.info('Copying %s to %s', file_from, file_to)
        subprocess.check_call(['cp', file_from, file_to])

if __name__ == "__main__":
    assert len(sys.argv) >= 2
    process_file(sys.argv[1])
