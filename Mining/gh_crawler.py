import requests
import sys
import time

# Insert GitHub API token here, in place of *TOKEN*.
headers = {"Authorization": "token *TOKEN*"}

# Constants & language argument.
NUM_REPOS = 25_000
MIN_STARS = 50
LAST_ACTIVE = '2020-01-01'
LANGUAGE = "java" if len(sys.argv) <= 1 else sys.argv[1]  # Default to Java, if none passed.

def main():
	repositories = set()  # Keep track of a set of repositories seen to avoid duplicate entries across pages.
	next_max_stars = 1_000_000_000  # Initialize to a very high value.
	with open(f'TopLists/{LANGUAGE}-top-repos.txt', 'w') as f:
		while len(repositories) < NUM_REPOS:
			results = run_query(next_max_stars)  # Get the next set of pages.
			if not results:
				break
			new_repositories = [repository for repository, _ in results]
			next_max_stars = min([stars for _, stars in results])
			
			# If a query returns no new repositories, drop it.
			if len(repositories | set(new_repositories)) == len(repositories):
				break
			for repository, stars in sorted(results, key=lambda e: e[1], reverse=True):
				if repository not in repositories:
					repositories.add(repository)
					f.write(f'{stars}\t{repository}\n')
			f.flush()
			print(f'Collected {len(repositories):,} repositories so far; lowest number of stars: {next_max_stars:,}')


def run_query(max_stars):
	end_cursor = None  # Used to track pagination.
	repositories = set()
	
	while end_cursor != "":
		# Extracts non-fork, recently active repositories in the provided language, in groups of 100.
		# Leaves placeholders for maximum stars and page cursor. The former allows us to retrieve more than 1,000 repositories
		# by repeatedly lowering the bar.
		query = f"""
		{{
		  search(query: "language:{LANGUAGE} fork:false pushed:>{LAST_ACTIVE} sort:stars stars:<{max_stars}", type: REPOSITORY, first: 100 {', after: "' + end_cursor + '"' if end_cursor else ''}) {{
			edges {{
			  node {{
				... on Repository {{
				  url
				  isPrivate
				  isDisabled
				  isLocked
				  stargazers {{
					totalCount
				  }}
				}}
			  }}
			}}
			pageInfo {{
			  hasNextPage
			  endCursor
			}}
		  }}
		}}
		"""
		print(f'  Retrieving next page; {len(repositories)} repositories in this batch so far.')
		# Attempt a query up to three times, pausing when a query limit is hit.
		attempts = 0
		success = False
		while not success and attempts < 3:
			request = requests.post('https://api.github.com/graphql', json={'query': query}, headers=headers)
			content = request.json()
			if 'data' not in content or 'search' not in content['data']:
				# If this is simply a signal to pause querying, wait two minutes.
				if 'message' in content and 'wait' in content['message']:
					attempts += 1
					time.sleep(120)
				# Otherwise, assume we've hit the end of the stream.
				else:
					break
			else:
				success = True
		if not success:
			break
		end_cursor = get_end_cursor(content)
		new_repositories, is_done = get_repositories(content)
		repositories.update(new_repositories)
		if len(repositories) > NUM_REPOS or is_done:
			break
	return repositories


def get_end_cursor(content):
	page_info = content['data']['search']['pageInfo']
	has_next_page = page_info['hasNextPage']
	if has_next_page:
		return page_info['endCursor']
	return ""


def get_repositories(content):
	edges = content['data']['search']['edges']
	repositories_with_stars = []
	for edge in edges:
		if edge['node']['isPrivate'] is False and edge['node']['isDisabled'] is False and edge['node']['isLocked'] is False:
			repository = edge['node']['url']
			star_count = edge['node']['stargazers']['totalCount']
			if star_count < MIN_STARS:
				return repositories_with_stars, True
			repositories_with_stars.append((repository, star_count))
	return repositories_with_stars, False


if __name__ == '__main__':
	main()
