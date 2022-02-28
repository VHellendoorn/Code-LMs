#include <iostream>
#include <vector>
#include <string>
#include <algorithm>
#include <cstdio>
#include <queue>
using namespace std;
struct TreeNode {
	int val;
	TreeNode *left;
	TreeNode *right;
	TreeNode(int x) : val(x), left(nullptr), right(nullptr){}
};
class Solution {
public:
	TreeNode * invertTree(TreeNode *root) {
		if (root == nullptr)
			return nullptr;
		TreeNode *left = root->left;
		root->left = invertTree(root->right);
		root->right = invertTree(left);
		return root;
	}
};
TreeNode *mk_node(int val)
{
	return new TreeNode(val);
}
TreeNode *mk_child(TreeNode *root, TreeNode *left, TreeNode *right)
{
	root->left = left;
	root->right = right;
	return root;
}
TreeNode *mk_child(TreeNode *root, int left, int right)
{
	return mk_child(root, new TreeNode(left), new TreeNode(right));
}
TreeNode *mk_child(int root, int left, int right)
{
	return mk_child(new TreeNode(root), new TreeNode(left), new TreeNode(right));
}
void print(TreeNode *root)
{
	if (root == nullptr)
		return;
	print(root->left);
	cout << root->val << endl;
	print(root->right);
}
int main(int argc, char **argv)
{
	Solution solution;
	TreeNode *root = mk_child(4, 2, 7);
	mk_child(root->left, 1, 3);
	mk_child(root->right, 6, 9);
	solution.invertTree(root);
	print(root);
	return 0;
}
