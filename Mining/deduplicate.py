import hashlib
import os

ROOT = 'Code'  # NOTE: hard-coded.
seen = set()
count = 0
dups = 0

for root_dir, _, files in os.walk(ROOT):
	for file in files:
		count += 1
		file_path = os.path.join(root_dir, file)
		# Hash the entire file's content.
		with open(file_path, 'rb') as f:
			bytes = f.read()
			hash = hashlib.sha256(bytes).hexdigest()

		# Delete identical files.
		if hash in seen:
			os.remove(file_path)
			dups += 1
		else:
			seen.add(hash)

		# Periodically print progress and the running duplication ratio.
		if count % 10000 == 0:
			print(f'Processed {count:,} files, duplicates so far: {dups:,} ({dups/count:.1%})')
