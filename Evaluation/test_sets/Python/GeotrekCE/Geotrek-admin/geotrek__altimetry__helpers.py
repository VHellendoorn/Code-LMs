import logging

from django.contrib.gis.geos import GEOSGeometry
from django.utils import translation
from django.utils.translation import gettext as _
from django.contrib.gis.geos import LineString
from django.conf import settings
from django.db import connection

import pygal
from pygal.style import LightSolarizedStyle

logger = logging.getLogger(__name__)


class AltimetryHelper:
    @classmethod
    def elevation_profile(cls, geometry3d, precision=None, offset=0):
        """Extract elevation profile from a 3D geometry.

        :precision:  geometry sampling in meters
        """
        precision = precision or settings.ALTIMETRIC_PROFILE_PRECISION

        if geometry3d.geom_type == 'Point':
            return [[0, geometry3d.x, geometry3d.y, geometry3d.z]]

        if geometry3d.geom_type == 'MultiLineString':
            profile = []
            for subcoords in geometry3d.coords:
                subline = LineString(subcoords, srid=geometry3d.srid)
                offset += subline.length
                subprofile = AltimetryHelper.elevation_profile(subline, precision, offset)
                profile.extend(subprofile)
            return profile

        # Add measure to 2D version of geometry3d
        # Get distance from origin for each vertex
        sql = """
        WITH line2d AS (SELECT ST_Force2D('%(ewkt)s'::geometry) AS geom),
             line_measure AS (SELECT ST_Addmeasure(geom, 0, ST_length(geom)) AS geom FROM line2d),
             points2dm AS (SELECT (ST_DumpPoints(geom)).geom AS point FROM line_measure)
        SELECT (%(offset)s + ST_M(point)) FROM points2dm;
        """ % {'offset': offset, 'ewkt': geometry3d.ewkt}
        cursor = connection.cursor()
        cursor.execute(sql)
        pointsm = cursor.fetchall()
        # Join (offset+distance, x, y, z) together
        geom3dapi = geometry3d.transform(settings.API_SRID, clone=True)
        assert len(pointsm) == len(geom3dapi.coords), 'Cannot map distance to xyz'
        dxyz = [pointsm[i] + v for i, v in enumerate(geom3dapi.coords)]
        return dxyz

    @classmethod
    def altimetry_limits(cls, profile):
        elevations = [int(v[3]) for v in profile]
        min_elevation = int(min(elevations))
        max_elevation = int(max(elevations))
        floor_elevation = round(min_elevation, 100) - 100
        ceil_elevation = round(max_elevation, 100) + 100
        if ceil_elevation < floor_elevation + settings.ALTIMETRIC_PROFILE_MIN_YSCALE:
            ceil_elevation = floor_elevation + settings.ALTIMETRIC_PROFILE_MIN_YSCALE

        return ceil_elevation, floor_elevation

    @classmethod
    def profile_svg(cls, profile, language):
        """
        Plot the altimetric graph in SVG using PyGal.
        Most of the job done here is dedicated to preparing
        nice labels scales.
        """
        ceil_elevation, floor_elevation = cls.altimetry_limits(profile)
        config = dict(show_legend=False,
                      print_values=False,
                      show_dots=False,
                      zero=floor_elevation,
                      value_formatter=lambda v: '%d' % v,
                      margin=settings.ALTIMETRIC_PROFILE_FONTSIZE,
                      width=settings.ALTIMETRIC_PROFILE_WIDTH,
                      height=settings.ALTIMETRIC_PROFILE_HEIGHT,
                      title_font_size=settings.ALTIMETRIC_PROFILE_FONTSIZE,
                      label_font_size=0.8 * settings.ALTIMETRIC_PROFILE_FONTSIZE,
                      major_label_font_size=settings.ALTIMETRIC_PROFILE_FONTSIZE,
                      js=[])

        style = LightSolarizedStyle
        style.background = settings.ALTIMETRIC_PROFILE_BACKGROUND
        style.colors = (settings.ALTIMETRIC_PROFILE_COLOR,)
        style.font_family = settings.ALTIMETRIC_PROFILE_FONT
        line_chart = pygal.XY(fill=True, style=style, **config)
        if language:
            translation.activate(language)
        line_chart.x_title = _("Distance (m)")
        line_chart.y_title = _("Altitude (m)")
        line_chart.show_minor_x_labels = False
        line_chart.x_labels_major_count = 5
        line_chart.show_minor_y_labels = False
        line_chart.truncate_label = 50
        line_chart.range = [floor_elevation, ceil_elevation]
        line_chart.no_data_text = _("Altimetry data not available")
        translation.deactivate()
        line_chart.add('', [(int(v[0]), int(v[3])) for v in profile])
        return line_chart.render()

    @classmethod
    def _nice_extent(cls, geom):
        xmin, ymin, xmax, ymax = geom.extent
        amplitude = max(xmax - xmin, ymax - ymin)
        geom_buffer = geom.envelope.buffer(amplitude * settings.ALTIMETRIC_AREA_MARGIN)
        xmin, ymin, xmax, ymax = geom_buffer.extent
        width = xmax - xmin
        height = ymax - ymin
        xcenter = xmin + width / 2.0
        ycenter = ymin + height / 2.0

        min_ratio = 1 / 1.618  # golden ratio
        if width > height:
            height = max(width * min_ratio, height)
        else:
            width = max(height * min_ratio, width)
        xmin, ymin, xmax, ymax = (int(xcenter - width / 2.0),
                                  int(ycenter - height / 2.0),
                                  int(xcenter + width / 2.0),
                                  int(ycenter + height / 2.0))
        return (xmin, ymin, xmax, ymax)

    @classmethod
    def elevation_area(cls, geom):
        xmin, ymin, xmax, ymax = cls._nice_extent(geom)
        width = xmax - xmin
        height = ymax - ymin
        precision = settings.ALTIMETRIC_PROFILE_PRECISION
        max_resolution = settings.ALTIMETRIC_AREA_MAX_RESOLUTION
        if width / precision > max_resolution:
            precision = int(width / max_resolution)
        if height / precision > 10000:
            precision = int(width / max_resolution)
        if height < precision or width < precision:
            precision = min([height, width])

        sql = """
            -- Author: Celian Garcia
            WITH columns AS (
                    SELECT generate_series({xmin}::int, {xmax}::int, {precision}) AS x
                ),
                lines AS (
                    SELECT generate_series({ymin}::int, {ymax}::int, {precision}) AS y
                ),
                resolution AS (
                    SELECT x, y
                    FROM (SELECT COUNT(x) AS x FROM columns) AS col,
                         (SELECT COUNT(y) AS y FROM lines)   AS lin
                ),
                points2d AS (
                    SELECT row_number() OVER (ORDER BY lines.y, columns.x) AS id,
                           ST_SetSRID(ST_MakePoint(x, y), {srid}) AS geom,
                           ST_Transform(ST_SetSRID(ST_MakePoint(x, y), {srid}), 4326) AS geomll
                    FROM columns, lines
                ),
                draped AS (
                    SELECT id, CASE WHEN ST_Value(altimetry_dem.rast, p.geom)::int = -99999 THEN 0 ELSE ST_Value(altimetry_dem.rast, p.geom)::int END AS altitude
                    FROM altimetry_dem, points2d AS p
                    WHERE ST_Intersects(altimetry_dem.rast, p.geom)
                ),
                all_draped AS (
                    SELECT geomll, geom, altitude
                    FROM points2d LEFT JOIN draped ON (points2d.id = draped.id)
                    ORDER BY points2d.id
                ),
                extent_latlng AS (
                    SELECT ST_Envelope(ST_Union(geom)) AS extent,
                           MIN(altitude) AS min_z,
                           MAX(altitude) AS max_z,
                           AVG(altitude) AS center_z
                    FROM all_draped
                )
            SELECT extent,
                   ST_transform(extent, 4326),
                   center_z,
                   min_z,
                   max_z,
                   resolution.x AS resolution_w,
                   resolution.y AS resolution_h,
                   altitude
            FROM extent_latlng, resolution, all_draped;
        """.format(xmin=xmin, ymin=ymin, xmax=xmax, ymax=ymax,
                   srid=settings.SRID, precision=precision)
        cursor = connection.cursor()
        cursor.execute(sql)
        result = cursor.fetchall()
        first = result[0]
        envelop_native, envelop, center_z, min_z, max_z, resolution_w, resolution_h, a = first
        envelop = GEOSGeometry(envelop, srid=4326)
        envelop_native = GEOSGeometry(envelop_native, srid=settings.SRID)

        if center_z is None:
            logger.warning("No DEM present")
            return {}

        altitudes = []
        row = []
        for i, record in enumerate(result):
            if i > 0 and i % resolution_w == 0:
                altitudes.append(row)
                row = []
            elevation = (record[7] or 0.0) - min_z
            row.append(elevation)
        altitudes.append(row)

        area = {
            'center': {
                'x': envelop_native.centroid.x,
                'y': envelop_native.centroid.y,
                'lat': envelop.centroid.y,
                'lng': envelop.centroid.x,
                'z': int(center_z)
            },
            'resolution': {
                'x': resolution_w,
                'y': resolution_h,
                'step': precision
            },
            'size': {
                'x': envelop_native.coords[0][2][0] - envelop_native.coords[0][0][0],
                'y': envelop_native.coords[0][2][1] - envelop_native.coords[0][0][1],
                'lat': envelop.coords[0][2][0] - envelop.coords[0][0][0],
                'lng': envelop.coords[0][2][1] - envelop.coords[0][0][1]
            },
            'extent': {
                'altitudes': {
                    'min': min_z,
                    'max': max_z
                },
                'southwest': {'lat': envelop.coords[0][0][1],
                              'lng': envelop.coords[0][0][0],
                              'x': envelop_native.coords[0][0][0],
                              'y': envelop_native.coords[0][0][1]},
                'northwest': {'lat': envelop.coords[0][1][1],
                              'lng': envelop.coords[0][1][0],
                              'x': envelop_native.coords[0][1][0],
                              'y': envelop_native.coords[0][1][1]},
                'northeast': {'lat': envelop.coords[0][2][1],
                              'lng': envelop.coords[0][2][0],
                              'x': envelop_native.coords[0][2][0],
                              'y': envelop_native.coords[0][2][1]},
                'southeast': {'lat': envelop.coords[0][3][1],
                              'lng': envelop.coords[0][3][0],
                              'x': envelop_native.coords[0][3][0],
                              'y': envelop_native.coords[0][3][1]}
            },
            'altitudes': altitudes
        }
        return area
