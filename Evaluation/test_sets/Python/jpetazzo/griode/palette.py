# The file palette.yaml contains two sections:
# PALETTES
# COLORS
#
# PALETTES contains color maps for various devices.
# Currently, there is RGB and RG (the latter is for the
# Launchpad Mini and Launchpad S, which only have red
# and green leds, and cannot achieve a full palette).
#
# COLORS contains arrays of names of colors to be used
# in the code. For each virtual color, there is one
# entry per palette, indicating how to render that
# virtual color with the corresponding palette.
# For instance:
# POWERINDICATOR:
#   RGB: [ RED, GREEN ]
#   RG : [ R3G0, R0G3 ]
# In the code, we would do:
#
# from palette import palette
# grid[(x,y)] = palette.POWERINDICATOR[0]
# 
# When rendering on a RGB device, it will use RED,
# and when rendering on a RG device, it will use R3G0.

import yaml


class Palette(object):

	def __init__(self, data):
		for color_name, color_data in data["COLORS"].items():
			cycle = dict()
			# Store the color name.
			# (We don't use it, but it could help with debugging.)
			cycle[""] = color_name
			for real_palette_name, real_colors_names in color_data.items():
				for i, real_color_name in enumerate(real_colors_names):
					if i not in cycle:
						cycle[i] = dict()
						cycle[i][""] = i
					real_palette = data["PALETTES"][real_palette_name]
					cycle[i][real_palette_name] = real_palette[real_color_name]
			# This allows to use palette.FOO to access palette.FOO[0]
			for palette in cycle[0]:
				if palette != "":
					cycle[palette] = cycle[0][palette]
			setattr(self, color_name, cycle)


data = yaml.safe_load(open("palette.yaml"))

palette = Palette(data)

def test():
	print("palette.ROOT[0][RGB] = ", palette.ROOT[0]["RGB"])
	print("palette.MENU[1][RG] = ", palette.MENU[1]["RG"])

if __name__ == "__main__":
	test()
