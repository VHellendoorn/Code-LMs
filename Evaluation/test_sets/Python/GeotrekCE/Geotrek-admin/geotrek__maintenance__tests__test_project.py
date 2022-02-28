from django.test import TestCase
from django.conf import settings

from unittest import skipIf

from geotrek.infrastructure.tests.factories import InfrastructureFactory
from geotrek.signage.tests.factories import SignageFactory
from geotrek.maintenance.tests.factories import InterventionFactory, ProjectFactory
from geotrek.core.tests.factories import TopologyFactory
from geotrek.land.tests.factories import (SignageManagementEdgeFactory, WorkManagementEdgeFactory,
                                          CompetenceEdgeFactory)


class ProjectTest(TestCase):
    @skipIf(not settings.TREKKING_TOPOLOGY_ENABLED, 'Test with dynamic segmentation only')
    def test_helpers(self):
        i1 = InterventionFactory.create(target=None)
        i2 = InterventionFactory.create(target=None)
        i3 = InterventionFactory.create(target=None)
        sign = SignageFactory.create()
        i1.target = sign
        i1.save()

        p1 = sign.paths.get()

        infra = InfrastructureFactory.create()
        i2.target = infra
        i2.save()
        p2 = infra.paths.get()

        t = TopologyFactory.create(paths=[p1])
        i3.target = t

        proj = ProjectFactory.create()
        self.assertCountEqual(proj.paths.all(), [])
        self.assertEqual(proj.signages, [])
        self.assertEqual(proj.infrastructures, [])

        i1.save()

        proj.interventions.add(i1)
        self.assertCountEqual(proj.paths.all(), [p1])
        self.assertEqual(proj.signages, [sign])
        self.assertEqual(proj.infrastructures, [])

        i2.save()

        proj.interventions.add(i2)
        self.assertCountEqual(proj.paths.all(), [p1, p2])
        self.assertEqual(proj.signages, [sign])
        self.assertEqual(proj.infrastructures, [infra])

        i3.save()

        proj.interventions.add(i3)
        self.assertCountEqual(proj.paths.all(), [p1, p2])
        self.assertEqual(proj.signages, [sign])
        self.assertEqual(proj.infrastructures, [infra])

    @skipIf(settings.TREKKING_TOPOLOGY_ENABLED, 'Test without dynamic segmentation only')
    def test_helpers_nds(self):
        i1 = InterventionFactory.create(target=None)
        i2 = InterventionFactory.create(target=None)
        i3 = InterventionFactory.create(target=None)
        sign = SignageFactory.create(geom="SRID=4326;POINT(0 5)")
        i1.target = sign
        i1.save()

        infra = InfrastructureFactory.create(geom="SRID=4326;POINT(1 5)")
        i2.target = infra
        i2.save()

        t = TopologyFactory.create(geom="SRID=4326;POINT(2 5)")
        i3.target = t
        i3.save()

        proj = ProjectFactory.create()
        self.assertCountEqual(proj.paths.all(), [])
        self.assertEqual(proj.signages, [])
        self.assertEqual(proj.infrastructures, [])

        i1.save()

        proj.interventions.add(i1)
        self.assertEqual(proj.signages, [sign])
        self.assertEqual(proj.infrastructures, [])

        i2.save()

        proj.interventions.add(i2)
        self.assertEqual(proj.signages, [sign])
        self.assertEqual(proj.infrastructures, [infra])

        i3.save()

        proj.interventions.add(i3)
        self.assertEqual(proj.signages, [sign])
        self.assertEqual(proj.infrastructures, [infra])

    def test_deleted_intervention(self):
        sign = SignageFactory.create()
        i1 = InterventionFactory.create(target=sign)

        proj = ProjectFactory.create()
        proj.interventions.add(i1)
        self.assertEqual(proj.signages, [sign])
        i1.delete()
        self.assertEqual(proj.signages, [])

    def test_deleted_infrastructure(self):
        infra = InfrastructureFactory.create()
        i1 = InterventionFactory.create(target=infra)

        proj = ProjectFactory.create()
        proj.interventions.add(i1)
        self.assertEqual(proj.infrastructures, [infra])

        infra.delete()

        self.assertEqual(proj.infrastructures, [])


@skipIf(not settings.TREKKING_TOPOLOGY_ENABLED, 'Test with dynamic segmentation only')
class ProjectLandTest(TestCase):
    def setUp(self):
        infra = InfrastructureFactory.create()
        self.intervention = InterventionFactory.create(target=infra)
        self.project = ProjectFactory.create()
        self.project.interventions.add(self.intervention)
        self.project.interventions.add(InterventionFactory.create())

        path = infra.paths.get()

        self.signagemgt = SignageManagementEdgeFactory.create(paths=[(path, 0.3, 0.7)])
        self.workmgt = WorkManagementEdgeFactory.create(paths=[(path, 0.3, 0.7)])
        self.competencemgt = CompetenceEdgeFactory.create(paths=[(path, 0.3, 0.7)])

    def test_project_has_signage_management(self):
        self.assertIn(self.signagemgt, self.intervention.signage_edges)
        self.assertIn(self.signagemgt, self.project.signage_edges)

    def test_project_has_work_management(self):
        self.assertIn(self.workmgt, self.intervention.work_edges)
        self.assertIn(self.workmgt, self.project.work_edges)

    def test_project_has_competence_management(self):
        self.assertIn(self.competencemgt, self.intervention.competence_edges)
        self.assertIn(self.competencemgt, self.project.competence_edges)
