from unittest import skipIf

from django.conf import settings
from django.test import TestCase

from . import factories


class CoreFactoriesTest(TestCase):
    """
    Ensure factories work as expected.
    Here we just call each one to ensure they do not trigger any random
    error without verifying any other expectation.
    """

    @skipIf(not settings.TREKKING_TOPOLOGY_ENABLED, 'Test with dynamic segmentation only')
    def test_path_factory(self):
        factories.PathFactory()

    def test_topology_mixin_factory(self):
        factories.TopologyFactory()

    @skipIf(not settings.TREKKING_TOPOLOGY_ENABLED, 'Test with dynamic segmentation only')
    def test_path_aggregation_factory(self):
        factories.PathAggregationFactory()

    @skipIf(not settings.TREKKING_TOPOLOGY_ENABLED, 'Test with dynamic segmentation only')
    def test_source_management_factory(self):
        factories.PathSourceFactory()

    @skipIf(not settings.TREKKING_TOPOLOGY_ENABLED, 'Test with dynamic segmentation only')
    def test_challenge_management_factory(self):
        factories.StakeFactory()

    @skipIf(not settings.TREKKING_TOPOLOGY_ENABLED, 'Test with dynamic segmentation only')
    def test_usage_management_factory(self):
        factories.UsageFactory()

    @skipIf(not settings.TREKKING_TOPOLOGY_ENABLED, 'Test with dynamic segmentation only')
    def test_network_management_factory(self):
        factories.NetworkFactory()

    @skipIf(not settings.TREKKING_TOPOLOGY_ENABLED, 'Test with dynamic segmentation only')
    def test_path_management_factory(self):
        factories.TrailFactory()

    @skipIf(not settings.TREKKING_TOPOLOGY_ENABLED, 'Test with dynamic segmentation only')
    def test_path_in_bounds_existing_factory(self):
        factories.PathFactory.create()
        factories.PathInBoundsExistingGeomFactory()

    @skipIf(not settings.TREKKING_TOPOLOGY_ENABLED, 'Test with dynamic segmentation only')
    def test_path_in_bounds_not_existing_factory(self):
        with self.assertRaises(IndexError):
            factories.PathInBoundsExistingGeomFactory()
