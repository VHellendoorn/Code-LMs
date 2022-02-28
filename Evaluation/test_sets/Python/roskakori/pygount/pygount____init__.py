"""
Pygount counts lines of source code using pygments lexers.
"""
# Copyright (c) 2016-2022, Thomas Aglassinger.
# All rights reserved. Distributed under the BSD License.
import pkg_resources

from .analysis import DuplicatePool, SourceAnalysis, SourceScanner, SourceState, encoding_for, source_analysis
from .common import Error, OptionError
from .summary import LanguageSummary, ProjectSummary

__version__ = pkg_resources.get_distribution(__name__).version

__all__ = [
    "__version__",
    "encoding_for",
    "DuplicatePool",
    "Error",
    "LanguageSummary",
    "OptionError",
    "ProjectSummary",
    "SourceAnalysis",
    "SourceScanner",
    "SourceState",
    "source_analysis",
]
