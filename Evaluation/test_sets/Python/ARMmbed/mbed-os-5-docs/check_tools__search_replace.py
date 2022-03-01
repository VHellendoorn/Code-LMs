#!/usr/bin/env python
# Copyright (c) 2020 Arm Limited and Contributors. All rights reserved.
#

"""Script to search for a pattern a replace with something else."""

import argparse
import json
import logging
import os
import pathlib
import re
import sys
from enum import Enum
from shutil import move, copymode
from tempfile import mkstemp


class ReturnCode(Enum):
    """Return codes."""

    SUCCESS = 0
    ERROR = 1
    INVALID_OPTIONS = 2


LOG = logging.getLogger(__name__)

LOG_FORMAT = "%(asctime)s - %(levelname)s - %(message)s"

SUPPORTED_FILE_EXTENSIONS = ("md", "txt")

DEFAULT_CONFIG_FILE = os.path.join(
    pathlib.Path(__file__).parent.absolute(), "search_replace.json"
)


class ConfigException(Exception):
    """Config file could not be found or is missing configurations."""


class ArgumentParserWithDefaultHelp(argparse.ArgumentParser):
    """Subclass that always shows the help message on invalid arguments."""

    def error(self, message):
        """Error handler."""
        sys.stderr.write(f"error: {message}\n")
        self.print_help()
        raise SystemExit(ReturnCode.INVALID_OPTIONS.value)


def set_log_verbosity(increase_verbosity):
    """Set the verbosity of the log output."""
    log_level = logging.DEBUG if increase_verbosity else logging.INFO

    LOG.setLevel(log_level)
    logging.basicConfig(level=log_level, format=LOG_FORMAT)


def get_search_replace_patterns(config_file):
    """Get the search and replace patterns."""
    try:
        with open(config_file, "r") as f:
            configs = json.load(f)
        return configs["search"], configs["replace"]
    except KeyError:
        raise ConfigException(ConfigException().__doc__)

def get_insert_tags(config_file):
    """Get the insert tags."""
    try:
        with open(config_file, "r") as f:
            configs = json.load(f)
        return (
            configs["insert_start_tag"],
            configs["insert_end_tag"],
            configs["insert_string"]
        )
    except KeyError:
        raise ConfigException(ConfigException().__doc__)

def insert_string(matchobj):
    """Insert string between two tags."""
    start_tag, end_tag, string_to_insert = get_insert_tags(DEFAULT_CONFIG_FILE)
    re.search(start_tag, matchobj.group(0))
    return (
        re.search(start_tag, matchobj.group(0)).group(0)
        + string_to_insert
        + end_tag
    )


def replace_pattern_in_file(file_path, pattern, substitute):
    """Replace a pattern in a file."""
    LOG.debug(f"Processing {file_path}...")
    if not file_path.lower().endswith(SUPPORTED_FILE_EXTENSIONS):
        LOG.debug("This file extension is not supported.")
        return
    fh, abs_path = mkstemp()
    with os.fdopen(fh, "w") as new_file:
        with open(file_path) as old_file:
            old_file_content = old_file.read()
        new_file_content = re.sub(pattern, substitute, old_file_content)
        new_file.write(new_file_content)
    copymode(file_path, abs_path)
    os.remove(file_path)
    move(abs_path, file_path)


def search_replace(args):
    """Search for a pattern in the file(s) and replace it."""

    if args.insert:
        start_tag, end_tag, _ = get_insert_tags(DEFAULT_CONFIG_FILE)
        search_pattern = start_tag + end_tag
        substitute = insert_string
    else:
        search_pattern, substitute = get_search_replace_patterns(DEFAULT_CONFIG_FILE)

    if os.path.isfile(args.path):
        replace_pattern_in_file(args.path, search_pattern, substitute)
    else:
        for (dirpath, _, filenames) in os.walk(args.path):
            for filename in filenames:
                replace_pattern_in_file(
                    os.path.join(dirpath, filename),
                    search_pattern,
                    substitute,
                )


def is_valid_file(parser, arg):
    """Check if the file or directory exists."""
    file = os.path.join(pathlib.Path().absolute(), arg)
    if not os.path.exists(file):
        parser.error(f"'{file}': No such file or directory.")
        pass
    else:
        return file


def parse_args():
    """Parse the command line args."""
    parser = ArgumentParserWithDefaultHelp(
        description=("Search for a pattern and replace it."),
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )

    parser.add_argument(
        "path",
        type=lambda x: is_valid_file(parser, x),
        help=(
            "path to file or directory of files to look"
            " for the pattern to replace. Only supports the "
            f" following file extension: {SUPPORTED_FILE_EXTENSIONS}"
        ),
    )

    # TODO: Allow users to pass their own config files
    # parser.add_argument(
    #     "--config",
    #     type=lambda x: is_valid_file(parser, x),
    #     default=DEFAULT_CONFIG_FILE,
    #     help=(
    #         "configuration file that has the pattern to search"
    #         " and what to replace it with"
    #     ),
    # )

    parser.add_argument(
        "-v",
        "--verbose",
        action="store_true",
        help="increase verbosity of status information.",
    )

    parser.add_argument(
        "-i",
        "--insert",
        action="store_true",
        help=(
            "insert a string between a start tag and end tag defined in the"
            " configuration file."
        ),
    )

    parser.set_defaults(func=search_replace)

    args_namespace = parser.parse_args()

    # We want to fail gracefully, with a consistent
    # help message, in the no argument case.
    # So here's an obligatory hasattr hack.
    if not hasattr(args_namespace, "func"):
        parser.error("No arguments given!")
    else:
        return args_namespace


def run_search_replace():
    """Application main algorithm."""
    args = parse_args()

    set_log_verbosity(args.verbose)

    LOG.debug("Starting script")
    LOG.debug(f"Command line arguments:{args}")

    args.func(args)


def _main():
    """Run search and replace."""
    try:
        run_search_replace()
    except Exception as error:
        print(error)
        return ReturnCode.ERROR.value
    else:
        return ReturnCode.SUCCESS.value


if __name__ == "__main__":
    sys.exit(_main())
