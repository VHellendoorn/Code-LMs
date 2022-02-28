package resource

// List is a helper type for slices of resources.
// Although the length of the list is immutable, the content and the fields are not.
// Meaning, modifying the cells of one list may affect the cells of others.
type List []View

// With returns a new list with the provided resource appended at the end.
func (list List) With(res View) List {
	return append(list, res)
}

// Joined returns a new list which is the combination of this list and the provided one.
func (list List) Joined(other List) List {
	otherLen := len(other)
	if otherLen == 0 {
		return list
	}
	listLen := len(list)
	newList := make([]View, listLen+otherLen)
	copy(newList[:listLen], list)
	copy(newList[listLen:], other)
	return newList
}
