#include <cstdio>
#include <string>
#include <cstdlib>
#include <vector>
#include <iostream>
#include <cstring>
using namespace std;
class TrieNode {
	public:
		static const int SPACE_SIZE = 26;
		TrieNode *children[SPACE_SIZE];
		bool exist;

		TrieNode() {
			for (int i = 0; i < SPACE_SIZE; ++i) {
				children[i] = nullptr;
			}
			exist = false;
		}
};
class WordDictionary {
	public:
		WordDictionary() {
			root = new TrieNode();
		}
		void addWord(const string s) {
			TrieNode *p = root;
			for (char c : s) {
				int index = c - 'a';
				if (p->children[index] == nullptr) {
					p->children[index] = new TrieNode();
				}
				p = p->children[index];
			}
			p->exist = true;
		}
		void insert(const string s) {
			addWord(s);
		}
		bool search(const string key) const {
			return search(root, key.c_str());
		}
	private:
		TrieNode *root;
		bool search(TrieNode *p, const char *target) const {
			if (p == nullptr)
				return false;
			int len = strlen(target);
			if (target == nullptr || len == 0) {
				return p->exist;
			}
			char c = *target;
			if (c != '.') {
				int index = c - 'a';
				return search(p->children[index], target + 1);
			} else {
				for (int i = 0; i < TrieNode::SPACE_SIZE; ++i) {
					if (search(p->children[i], target + 1))
						return true;
				}
				return false;
			}
		}
};
int main(int argc, char **argv)
{
	WordDictionary trie;
	trie.insert("bad");
	trie.insert("dad");
	trie.insert("mad");
	cout << trie.search("") << endl;
	cout << trie.search("pad") << endl;
	cout << trie.search("bad") << endl;;
	cout << trie.search(".ad") << endl;
	cout << trie.search("b..") << endl;
	cout << trie.search("mada") << endl;
	return 0;
}
