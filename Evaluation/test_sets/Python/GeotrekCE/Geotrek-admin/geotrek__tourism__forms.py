from django.utils.translation import gettext_lazy as _

from .models import TouristicContent, TouristicEvent
from geotrek.common.forms import CommonForm

from crispy_forms.layout import Div


class TouristicContentForm(CommonForm):
    geomfields = ['geom']

    fieldslayout = [
        Div(
            'structure',
            'name',
            'category',
            'type1',
            'type2',
            'review',
            'published',
            'description_teaser',
            'description',
            'themes',
            'contact',
            'email',
            'website',
            'practical_info',
            'approved',
            'source',
            'portal',
            'eid',
            'reservation_system',
            'reservation_id'
        )
    ]

    class Meta:
        fields = ['structure', 'name', 'category', 'type1', 'type2', 'review', 'published',
                  'description_teaser', 'description', 'themes', 'contact',
                  'email', 'website', 'practical_info', 'approved', 'source', 'portal',
                  'geom', 'eid', 'reservation_system', 'reservation_id']
        model = TouristicContent

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        # Since we use chosen() in trek_form.html, we don't need the default help text
        for f in ['themes', 'type1', 'type2', 'source', 'portal']:
            self.fields[f].help_text = ''


class TouristicEventForm(CommonForm):
    geomfields = ['geom']

    fieldslayout = [
        Div(
            'structure',
            'name',
            'review',
            'published',
            'description_teaser',
            'description',
            'themes',
            'begin_date',
            'end_date',
            'duration',
            'meeting_point',
            'meeting_time',
            'contact',
            'email',
            'website',
            'organizer',
            'speaker',
            'type',
            'accessibility',
            'participant_number',
            'booking',
            'target_audience',
            'practical_info',
            'approved',
            'source',
            'portal',
            'eid',
        )
    ]

    class Meta:
        fields = ['name', 'review', 'published', 'description_teaser', 'description',
                  'themes', 'begin_date', 'end_date', 'duration', 'meeting_point',
                  'meeting_time', 'contact', 'email', 'website', 'organizer', 'speaker',
                  'type', 'accessibility', 'participant_number', 'booking', 'target_audience',
                  'practical_info', 'approved', 'source', 'portal', 'geom', 'eid', 'structure']
        model = TouristicEvent

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.fields['begin_date'].widget.attrs['placeholder'] = _('dd/mm/yyyy')
        self.fields['end_date'].widget.attrs['placeholder'] = _('dd/mm/yyyy')
        self.fields['meeting_time'].widget.attrs['placeholder'] = _('HH:MM')
        # Since we use chosen() in trek_form.html, we don't need the default help text
        for f in ['themes', 'source']:
            self.fields[f].help_text = ''
