from django.conf import settings

from modeltranslation.translator import translator, TranslationOptions

from geotrek.tourism import models as tourism_models


class InformationDeskTO(TranslationOptions):
    fields = ('name', 'description')


translator.register(tourism_models.InformationDesk, InformationDeskTO)


class InformationDeskTypeTO(TranslationOptions):
    fields = ('label',)


translator.register(tourism_models.InformationDeskType, InformationDeskTypeTO)


class TouristicContentTO(TranslationOptions):
    fields = ('name', 'description_teaser', 'description', 'practical_info'
              ) + (('published',) if settings.PUBLISHED_BY_LANG else tuple())
    fallback_undefined = {'published': None}


translator.register(tourism_models.TouristicContent, TouristicContentTO)


class TouristicContentCategoryTO(TranslationOptions):
    fields = ('label', 'type1_label', 'type2_label')


translator.register(tourism_models.TouristicContentCategory,
                    TouristicContentCategoryTO)


class TouristicContentTypeTO(TranslationOptions):
    fields = ('label',)


translator.register(tourism_models.TouristicContentType,
                    TouristicContentTypeTO)


# https://github.com/deschler/django-modeltranslation/issues/206#issuecomment-313228015
class TouristicContentSubTypeTO(TranslationOptions):
    fields = ()


translator.register(tourism_models.TouristicContentType1,
                    TouristicContentSubTypeTO)
translator.register(tourism_models.TouristicContentType2,
                    TouristicContentSubTypeTO)


class TouristicEventTypeTO(TranslationOptions):
    fields = ('type',)


translator.register(tourism_models.TouristicEventType, TouristicEventTypeTO)


class TouristicEventTO(TranslationOptions):
    fields = ('name', 'description_teaser', 'description', 'meeting_point',
              'accessibility', 'booking', 'practical_info', 'target_audience',
              ) + (('published',) if settings.PUBLISHED_BY_LANG else tuple())
    fallback_undefined = {'published': None}


translator.register(tourism_models.TouristicEvent, TouristicEventTO)
