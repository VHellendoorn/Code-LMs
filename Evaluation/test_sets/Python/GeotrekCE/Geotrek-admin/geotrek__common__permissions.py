from django.core.exceptions import PermissionDenied


class PublicOrReadPermMixin:

    def get_object(self, queryset=None):
        obj = super().get_object(queryset)
        if not obj.is_public():
            if not self.request.user.is_authenticated:
                raise PermissionDenied
            if not self.request.user.has_perm('%s.read_%s' % (obj._meta.app_label, obj._meta.model_name)):
                raise PermissionDenied
        return obj
