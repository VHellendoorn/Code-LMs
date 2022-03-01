import factory
from django.conf import settings
from django.contrib.gis.geos import Point

from .. import models
from geotrek.core.tests.factories import TopologyFactory, PointTopologyFactory
from geotrek.common.tests.factories import ReservationSystemFactory
from geotrek.common.utils.testdata import get_dummy_uploaded_image
from geotrek.infrastructure.tests.factories import InfrastructureFactory
from geotrek.signage.tests.factories import SignageFactory


class TrekNetworkFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = models.TrekNetwork

    network = "Network"
    pictogram = get_dummy_uploaded_image('network.png')


class PracticeFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = models.Practice

    name = "Usage"
    pictogram = get_dummy_uploaded_image('practice.png')


class RatingScaleFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = models.RatingScale

    name = "RatingScale"
    practice = factory.SubFactory(PracticeFactory)


class RatingFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = models.Rating

    name = "Rating"
    scale = factory.SubFactory(RatingScaleFactory)


class AccessibilityFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = models.Accessibility

    name = "Accessibility"
    pictogram = get_dummy_uploaded_image('accessibility.png')


class AccessibilityLevelFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = models.AccessibilityLevel

    name = "Easy"


class RouteFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = models.Route

    route = "Route"
    pictogram = get_dummy_uploaded_image('routes.png')


class DifficultyLevelFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = models.DifficultyLevel

    difficulty = "Difficulty"
    pictogram = get_dummy_uploaded_image('difficulty.png')


class WebLinkCategoryFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = models.WebLinkCategory

    label = "Category"
    pictogram = get_dummy_uploaded_image('weblink-category.png')


class WebLinkFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = models.WebLink

    name = "Web link"
    url = "http://dummy.url"
    category = factory.SubFactory(WebLinkCategoryFactory)


class TrekFactory(TopologyFactory):
    class Meta:
        model = models.Trek

    name = "Trek"
    departure = "Departure"
    arrival = "Arrival"
    published = True

    length = 10
    ascent = 0
    descent = 0
    min_elevation = 0
    max_elevation = 0

    description_teaser = "<p>Description teaser</p>"
    description = "<p>Description</p>"
    ambiance = "<p>Ambiance</p>"
    access = "<p>Access</p>"
    disabled_infrastructure = "<p>Disabled infrastructure</p>"
    duration = 1.5  # hour

    accessibility_level = factory.SubFactory(AccessibilityLevelFactory)
    accessibility_slope = "<p>Accessibility slope</p>"
    accessibility_signage = "<p>Accessibility signage</p>"
    accessibility_covering = "<p>Accessibility covering</p>"
    accessibility_exposure = "<p>Accessibility exposure</p>"
    accessibility_width = "<p>Accessibility width</p>"
    accessibility_advice = "<p>Accessibility advice</p>"
    advised_parking = "<p>Advised parking</p>"
    parking_location = Point(1, 1)

    public_transport = "<p>Public transport</p>"
    advice = "<p>Advice</p>"
    equipment = "<p>Equipment</p>"

    route = factory.SubFactory(RouteFactory)
    difficulty = factory.SubFactory(DifficultyLevelFactory)
    practice = factory.SubFactory(PracticeFactory)

    reservation_system = factory.SubFactory(ReservationSystemFactory)
    reservation_id = 'XXXXXXXXX'

    @factory.post_generation
    def web_links(obj, create, extracted=None, **kwargs):
        if create and extracted:
            obj.web_links.set(extracted)

    @factory.post_generation
    def sources(obj, create, extracted=None, **kwargs):
        if create and extracted:
            obj.source.set(extracted)

    @factory.post_generation
    def portals(obj, create, extracted=None, **kwargs):
        if create and extracted:
            obj.portal.set(extracted)


class TrekWithPOIsFactory(TrekFactory):
    @factory.post_generation
    def create_trek_with_poi(obj, create, extracted, **kwargs):
        if settings.TREKKING_TOPOLOGY_ENABLED:
            path = obj.paths.all()[0]
            POIFactory.create(paths=[(path, 0.5, 0.5)])
            POIFactory.create(paths=[(path, 0.4, 0.4)])
            if create:
                obj.save()
        else:
            POIFactory.create(geom='SRID=2154;POINT (700040 6600040)')
            POIFactory.create(geom='SRID=2154;POINT (700050 6600050)')


class TrekWithPublishedPOIsFactory(TrekFactory):
    @factory.post_generation
    def create_trek_with_poi(obj, create, extracted, **kwargs):
        if settings.TREKKING_TOPOLOGY_ENABLED:
            path = obj.paths.all()[0]
            POIFactory.create(paths=[(path, 0.5, 0.5)], published=True, published_en=True, published_fr=True)
            POIFactory.create(paths=[(path, 0.4, 0.4)], published=True, published_en=True, published_fr=True)
            if create:
                obj.save()
        else:
            POIFactory.create(geom='SRID=2154;POINT (700040 6600040)',
                              published=True, published_en=True, published_fr=True)
            POIFactory.create(geom='SRID=2154;POINT (700050 6600050)',
                              published=True, published_en=True, published_fr=True)


class TrekWithInfrastructuresFactory(TrekFactory):
    @factory.post_generation
    def create_trek_with_infrastructures(obj, create, extracted, **kwargs):
        if settings.TREKKING_TOPOLOGY_ENABLED:
            path = obj.paths.all()[0]
            InfrastructureFactory.create(paths=[(path, 0.5, 0.5)])
            InfrastructureFactory.create(paths=[(path, 0.4, 0.4)])
            if create:
                obj.save()
        else:
            InfrastructureFactory.create(geom='SRID=2154;POINT (700040 6600040)')
            InfrastructureFactory.create(geom='SRID=2154;POINT (700050 6600050)')


class TrekWithSignagesFactory(TrekFactory):
    @factory.post_generation
    def create_trek_with_infrastructures(obj, create, extracted, **kwargs):
        if settings.TREKKING_TOPOLOGY_ENABLED:
            path = obj.paths.all()[0]
            SignageFactory.create(paths=[(path, 0.5, 0.5)])
            SignageFactory.create(paths=[(path, 0.4, 0.4)])
            if create:
                obj.save()
        else:
            SignageFactory.create(geom='SRID=2154;POINT (700040 6600040)')
            SignageFactory.create(geom='SRID=2154;POINT (700050 6600050)')


class TrekWithServicesFactory(TrekFactory):
    @factory.post_generation
    def create_trek_with_services(obj, create, extracted, **kwargs):
        if settings.TREKKING_TOPOLOGY_ENABLED:
            path = obj.paths.all()[0]
            service1 = ServiceFactory.create(paths=[(path, 0.5, 0.5)])
            service1.type.practices.add(obj.practice)
            service2 = ServiceFactory.create(paths=[(path, 0.4, 0.4)])
            service2.type.practices.add(obj.practice)
            if create:
                obj.save()
        else:
            service1 = ServiceFactory.create(geom='SRID=2154;POINT (700040 6600040)')
            service1.type.practices.add(obj.practice)
            service2 = ServiceFactory.create(geom='SRID=2154;POINT (700050 6600050)')
            service2.type.practices.add(obj.practice)


class TrekRelationshipFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = models.TrekRelationship

    has_common_departure = False
    has_common_edge = False
    is_circuit_step = False

    trek_a = factory.SubFactory(TrekFactory)
    trek_b = factory.SubFactory(TrekFactory)


class POITypeFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = models.POIType

    label = "POI type"
    pictogram = get_dummy_uploaded_image('poi-type.png')


class POIFactory(PointTopologyFactory):
    class Meta:
        model = models.POI

    name = "POI"
    description = "<p>Description</p>"
    type = factory.SubFactory(POITypeFactory)
    published = True


class ServiceTypeFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = models.ServiceType

    name = "Service type"
    pictogram = get_dummy_uploaded_image('service-type.png')
    published = True


class ServiceFactory(PointTopologyFactory):
    class Meta:
        model = models.Service

    type = factory.SubFactory(ServiceTypeFactory)
