//********************************************************************
// Newton Game dynamics 
// copyright 2000-2004
// By Julio Jerez
// VC: 6.0
// simple demo list vector class with iterators
//********************************************************************

#ifndef __dList__
#define __dList__

#include "stdafx.h"


// this is a small double link list contatiner similar to STL, 
// it is used to probody low level support for this demos.
// it implements a iterators, and all the basics operation on list found on the SDL class

template<class T>
class dList 
{
	public:
	class dListNode
	{
		friend class dList<T>;
		dListNode *m_next;
		dListNode *m_prev;
		T m_info;

		dListNode (dListNode *prev, dListNode *next) 
			:m_info () 
		{
			m_prev = prev;
			m_next = next;
			if (m_prev) {
				m_prev->m_next = this;
			}
			if (m_next) {
				m_next->m_prev = this;
			}
		}

		dListNode (const T &info, dListNode *prev, dListNode *next) 
			:m_info (info) 
		{
			m_prev = prev;
			m_next = next;
			if (m_prev) {
				m_prev->m_next = this;
			}
			if (m_next) {
				m_next->m_prev = this;
			}
		}

		~dListNode()
		{
		}

		void Unlink ()
		{
			if (m_prev) {
				m_prev->m_next = m_next;
			}

			if (m_next) {
				m_next->m_prev = m_prev;
			}
			m_prev = NULL;
			m_next = NULL;
		}

		void Remove()
		{
			Unlink();
			delete this;
		}

		void AddLast(dListNode *node) 
		{
			m_next = node;
			node->m_prev = this;
		}

		void AddFirst(dListNode *node) 
		{
			m_prev = node;
			node->m_next = this;
		}

		void *operator new (size_t size) 
		{
			return malloc(size);
		}

		void operator delete (void *ptr) 
		{
			free(ptr);
		}

		public:
		T& GetInfo()
		{
			return m_info;
		}

		dListNode *GetNext() const
		{
			return m_next;
		}

		dListNode *GetPrev() const
		{
			return m_prev;
		}
	};

	class Iterator
	{
		dListNode *m_ptr;
		dList *m_list;

		public:
		Iterator (const dList<T> &me)
		{
			m_ptr = NULL;
			m_list = (dList *)&me;
		}

		~Iterator ()
		{
		}

		operator int() const
		{
			return m_ptr != NULL;
		}

		bool operator== (const Iterator &target) const
		{
			return (m_ptr == target.m_ptr) && (m_list == target.m_list);
		}

		void Begin()
		{
			m_ptr = m_list->GetFirst();
		}

		void End()
		{
			m_ptr = m_list->GetLast();
		}

		void Set (dListNode *node)
		{
			m_ptr = node;
		}

		void operator++ ()
		{
			m_ptr = m_ptr->m_next();
		}

		void operator++ (int)
		{
			m_ptr = m_ptr->GetNext();
		}

		void operator-- () 
		{
			m_ptr = m_ptr->GetPrev();
		}

		void operator-- (int) 
		{
			m_ptr = m_ptr->GetPrev();
		}

		T &operator* () const
		{
			return m_ptr->GetInfo();
		}

		dListNode *GetNode() const
		{
			return m_ptr;
		}
	};

	// ***********************************************************
	// member functions
	// ***********************************************************
	public:
	dList ();
	~dList ();

	void* operator new (size_t size);
	void operator delete (void *ptr);

	operator int() const;
	int GetCount() const;
	dListNode *GetLast() const;
	dListNode *GetFirst() const;
	void Append ();
	void Append (dListNode *node);
	void Append (const T &element);
	void Addtop ();
	void Addtop (dListNode *node);
	void Addtop (const T &element);
	void RotateToEnd (dListNode *node);
	void RotateToBegin (dListNode *node);
	void InsertAfter (dListNode *root, dListNode *node);

	dListNode *Find (const T &element) const;
	dListNode *GetNodeFromInfo (T &m_info) const;
	void Remove (dListNode *node);
	void Remove (const T &element);
	void RemoveAll ();
	

	// ***********************************************************
	// member variables
	// ***********************************************************
	private:
	int m_count;
	dListNode *m_last;
	dListNode *m_first;
	friend class dListNode;
};


template<class T>
dList<T>::dList ()
{
	m_count = 0;
	m_first = NULL;
	m_last = NULL;
}


template<class T>
dList<T>::~dList () 
{
	RemoveAll ();
}


template<class T>
void* dList<T>::operator new (size_t size)
{
	return malloc (size);
}

template<class T>
void dList<T>::operator delete (void *ptr)
{
	free (ptr);
}


template<class T>
int dList<T>::GetCount() const
{
	return m_count;
}

template<class T>
dList<T>::operator int() const
{
	return m_first != NULL;
}

template<class T>
typename dList<T>::dListNode *dList<T>::GetFirst() const
{
	return m_first;
}

template<class T>
typename dList<T>::dListNode *dList<T>::GetLast() const
{
	return m_last;
}

template<class T>
void dList<T>::Append (dListNode *node)
{
	m_count	++;
	if (m_first == NULL) {
		m_last = node;
		m_first = node;
	} else {
		m_last->AddLast (node);
		m_last = node;
	}
	node->AddRef();
	node->Unkill();

}

template<class T>
void dList<T>::Append ()
{
	m_count	++;
	if (m_first == NULL) {
		m_first = new dListNode(NULL, NULL);
		m_last = m_first;
	} else {
		m_last = new dListNode(m_last, NULL);
	}

}

template<class T>
void dList<T>::Append (const T &element)
{
	m_count	++;
	if (m_first == NULL) {
		m_first = new dListNode(element, NULL, NULL);
		m_last = m_first;
	} else {
		m_last = new dListNode(element, m_last, NULL);
	}

}

template<class T>
void dList<T>::Addtop (dListNode *node)
{
	m_count	++;
	if (m_last == NULL) {
		m_last = node;
		m_first = node;
	} else {
		m_first->AddFirst(node);
		m_first = node;
	}
	node->AddRef();
	node->Unkill();

}


template<class T>
void dList<T>::Addtop ()
{
	m_count	++;
	if (m_last == NULL) {
		m_last = new dListNode(NULL, NULL);
		m_first = m_last;
	} else {
		m_first = new dListNode(NULL, m_first);
	}

}


template<class T>
void dList<T>::Addtop (const T &element)
{
	m_count	++;
	if (m_last == NULL) {
		m_last = new dListNode(element, NULL, NULL);
		m_first = m_last;
	} else {
		m_first = new dListNode(element, NULL, m_first);
	}

}

template<class T>
void dList<T>::InsertAfter (dListNode *root, dListNode *node)
{

	node->m_prev = root;
	node->m_next = root->m_next;
	if (root->m_next) {
		root->m_next->m_prev = node;
	} 
	root->m_next = node;

	if (root == m_last) {
		m_last = node;
	}
}

template<class T>
void dList<T>::RotateToEnd (dListNode *node)
{
	if (node != m_last) {
		if (m_last != m_first) {
			if (node == m_first) {
				m_first = m_first->GetNext();
			}
			node->Unlink();
			m_last->AddLast(node);
			m_last = node; 
		}
	}
}

template<class T>
void dList<T>::RotateToBegin (dListNode *node)
{
	if (node != m_first) {
		if (m_last != m_first) {
			if (node == m_last) {
				m_last = m_last->GetPrev();
			}
			node->Unlink();
			m_first->AddFirst(node);
			m_first = node; 
		}
	}
}


template<class T>
typename dList<T>::dListNode *dList<T>::Find (const T &element) const
{
	dListNode *node;
	for (node = m_first; node; node = node->GetNext()) {
		if (element	== node->m_info) {
			break;
		}
	}
	return node;
}

/*
template<class T>
typename dList<T>::dListNode *dList<T>::GetNodeFromInfo (T &info) const
{
	dListNode *ptr;
	dgUnsigned64 offset;

	ptr = (dListNode *) &info;
	offset = dgUnsigned64 (&ptr->m_info) - dgUnsigned64 (ptr);
	ptr = (dListNode *) (dgUnsigned64 (&info) - offset);

	return ptr;
}
 */

template<class T> 
void dList<T>::Remove (const T &element)
{
	dListNode *node;

	node = Find (element);
	if (node) {
		Remove (node);
	}
}

template<class T>
void dList<T>::Remove (dListNode *node)
{
	m_count --;

	if (node == m_first) {
		m_first = m_first->GetNext();
	}
	if (node == m_last) {
		m_last = m_last->GetPrev();
	}
	node->Remove();
}

template<class T>
void dList<T>::RemoveAll ()
{
	dListNode *node;
	for (node = m_first; node; node = m_first) {
		m_count --;
		m_first = node->GetNext();
		node->Remove();
	}
	m_last = NULL;
	m_first = NULL;
}



#endif

