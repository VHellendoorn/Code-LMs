from django.utils.translation import gettext_lazy as _

from django_filters import RangeFilter, Filter, ModelMultipleChoiceFilter
from .fields import OneLineRangeField


class OptionalRangeFilter(RangeFilter):
    field_class = OneLineRangeField

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.field.fields[0].label = _('min %s') % self.field.label
        self.field.fields[1].label = _('max %s') % self.field.label


class ValueFilter(Filter):
    def do_filter(self, qs, value):
        return qs.filter(**{
            '%s' % self.field_name: value,
        }).distinct()

    def filter(self, qs, value):
        try:
            new_value = int(value)
        except (ValueError, TypeError):
            new_value = -1
        return qs if new_value < 0 else self.do_filter(qs, new_value)


class RightFilter(ModelMultipleChoiceFilter):
    model = None
    queryset = None

    def __init__(self, *args, **kwargs):
        kwargs.setdefault('queryset', self.get_queryset())
        super().__init__(*args, **kwargs)
        self.field.widget.attrs['class'] = self.field.widget.attrs.get('class', '') + 'right-filter'
        self.field.widget.renderer = None

    def get_queryset(self, request=None):
        if self.queryset is not None:
            return self.queryset
        return self.model.objects.all()
