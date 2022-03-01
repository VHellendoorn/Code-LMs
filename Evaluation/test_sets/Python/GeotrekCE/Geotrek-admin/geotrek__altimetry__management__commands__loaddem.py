from django.contrib.gis.gdal.error import GDALException
from django.core.management.base import BaseCommand, CommandError
from django.db import connection
from django.conf import settings
from django.contrib.gis.gdal import GDALRaster
import os.path
from subprocess import call, PIPE
import tempfile

from geotrek.altimetry.models import Dem


class Command(BaseCommand):
    help = 'Load DEM data (projecting and clipping it if necessary).\n'
    help += 'You may need to create a GDAL Virtual Raster if your DEM is '
    help += 'composed of several files.\n'
    can_import_settings = True

    def add_arguments(self, parser):
        parser.add_argument('dem_path')
        parser.add_argument('--replace', action='store_true', default=False, help='Replace existing DEM if any.')

    def handle(self, *args, **options):

        verbose = options['verbosity'] != 0

        try:
            cmd = 'raster2pgsql -G > /dev/null'
            kwargs_raster = {'shell': True}
            ret = self.call_command_system(cmd, **kwargs_raster)
            if ret != 0:
                raise Exception('raster2pgsql failed with exit code %d' % ret)
        except Exception as e:
            msg = 'Caught %s: %s' % (e.__class__.__name__, e,)
            raise CommandError(msg)
        if verbose:
            self.stdout.write('-- Checking input DEM ------------------\n')
        # Obtain DEM path
        dem_path = options['dem_path']

        # Open GDAL dataset
        if not os.path.exists(dem_path):
            raise CommandError('DEM file does not exists at: %s' % dem_path)
        try:
            rst = GDALRaster(dem_path, write=False)
        except GDALException:
            raise CommandError('DEM format is not recognized by GDAL.')

        # GDAL dataset check 1: ensure dataset has a known SRS
        if not rst.srs:
            raise CommandError('DEM coordinate system is unknown.')
        # Obtain dataset SRS
        if settings.SRID != rst.srs.srid:
            rst = rst.transform(settings.SRID)

        dem_exists = Dem.objects.exists()

        # Obtain replace mode
        replace = options['replace']

        # What to do with existing DEM (if any)
        if dem_exists and replace:
            # Drop table content
            Dem.objects.all().delete()
        elif dem_exists and not replace:
            raise CommandError('DEM file exists, use --replace to overwrite')

        if verbose:
            self.stdout.write('Everything looks fine, we can start loading DEM\n')

        output = tempfile.NamedTemporaryFile()  # SQL code for raster creation
        cmd = 'raster2pgsql -a -M -t 100x100 %s altimetry_dem %s' % (
            rst.name,
            '' if verbose else '2>/dev/null'
        )
        try:
            if verbose:
                self.stdout.write('\n-- Relaying to raster2pgsql ------------\n')
                self.stdout.write(cmd)
            kwargs_raster2 = {'shell': True, 'stdout': output.file, 'stderr': PIPE}
            ret = self.call_command_system(cmd, **kwargs_raster2)
            if ret != 0:
                raise Exception('raster2pgsql failed with exit code %d' % ret)
        except Exception as e:
            output.close()
            msg = 'Caught %s: %s' % (e.__class__.__name__, e,)
            raise CommandError(msg)

        if verbose:
            self.stdout.write('DEM successfully converted to SQL.\n')

        # Step 3: Dump SQL code into database
        if verbose:
            self.stdout.write('\n-- Loading DEM into database -----------\n')
        with connection.cursor() as cur:
            output.file.seek(0)
            for sql_line in output.file:
                cur.execute(sql_line)

        output.close()
        if verbose:
            self.stdout.write('DEM successfully loaded.\n')
        return

    def call_command_system(self, cmd, **kwargs):
        return_code = call(cmd, **kwargs)
        return return_code
