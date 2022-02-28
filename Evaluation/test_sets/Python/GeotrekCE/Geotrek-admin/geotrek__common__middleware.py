import re

from django.utils import translation
from django.utils.translation.trans_real import get_supported_language_variant

language_code_prefix_re = re.compile(r'^/api/([\w-]+)(/|$)')


def get_language_from_path(path):
    regex_match = language_code_prefix_re.match(path)
    if not regex_match:
        return None
    lang_code = regex_match.group(1)
    try:
        return get_supported_language_variant(lang_code)
    except LookupError:
        return None


class APILocaleMiddleware:
    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        language = get_language_from_path(request.path_info)
        if language:
            translation.activate(language)
            request.LANGUAGE_CODE = translation.get_language()
        return self.get_response(request)
