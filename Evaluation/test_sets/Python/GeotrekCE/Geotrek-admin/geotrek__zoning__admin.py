from django.contrib import admin
from django.utils.translation import gettext as _

from leaflet.admin import LeafletGeoAdmin

from geotrek.common.mixins import MergeActionMixin
from geotrek.zoning import models as zoning_models


def publish(modeladmin, request, queryset):
    queryset.update(published=True)


def unpublish(modeladmin, request, queryset):
    queryset.update(published=False)


publish.short_description = _("Publish (visible on Geotrek-rando)")
unpublish.short_description = _("Unpublish (hidden on Geotrek-rando)")


class RestrictedAreaTypeAdmin(MergeActionMixin, admin.ModelAdmin):
    search_fields = ('name',)
    list_display = ('name',)
    merge_field = 'name'


class CityAdmin(LeafletGeoAdmin):
    search_fields = ('code', 'name')
    list_display = ('name', 'code', 'published')
    list_filter = ('published', )
    actions = (publish, unpublish)


class RestrictedAreaAdmin(LeafletGeoAdmin):
    search_fields = ('name',)
    list_display = ('name', 'area_type', 'published')
    list_filter = ('area_type', 'published')
    actions = (publish, unpublish)


class DistrictAdmin(LeafletGeoAdmin):
    search_fields = ('name',)
    list_display = ('name', 'published')
    list_filter = ('published', )
    actions = (publish, unpublish)


admin.site.register(zoning_models.RestrictedAreaType, RestrictedAreaTypeAdmin)
admin.site.register(zoning_models.RestrictedArea, RestrictedAreaAdmin)
admin.site.register(zoning_models.City, CityAdmin)
admin.site.register(zoning_models.District, DistrictAdmin)
