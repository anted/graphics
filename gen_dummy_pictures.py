#!/usr/bin/env python
#
# -Creates 100 dummy team images
# -They need to go into images/pictures/
#

from subprocess import check_call

TEMPLATE = """
<svg height="600" width="800">
  <text x="150" y="100" fill="red" font-size="100" transform="rotate(30 20,40)">Team number {}</text>
</svg>
"""

for i in xrange(1, 100):
    file("{}.svg".format(i), "wt").write(TEMPLATE.format(i))
    check_call(["convert", "{}.svg".format(i), "{}.png".format(i)])

