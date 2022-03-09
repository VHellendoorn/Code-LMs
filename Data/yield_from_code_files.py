"""A drop-in replacement for `yield_from_files` in the gpt-neox tools/preprocess_data.py version which does not rely on lm_dataformat."""

import random
def yield_from_files(dir, semaphore):
    """
    Iterator over input documents, treated as plaintext.

    :param dir: directory to recursively extract files from.
	"""
    fnames = []
    for root, _, files in os.walk(dir):
        for file in files:
            fnames.append(os.path.join(root, file))
    random.shuffle(fnames)

    def read(fname):
        with open(fname) as inp:
            doc = inp.read()
        return doc

    def yielder(fname, semaphore):
        f = read(fname)
        if f:
            semaphore.acquire()
            yield f

    for fname in fnames:
        yield from yielder(fname, semaphore)