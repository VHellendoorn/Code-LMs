require_relative 'linked_list_implementation_amadou'

# Problem: Write a function, nth_node_from_end, that takes a head of a singly Linked List and a number n and returns the nth node from the end of the Linked List.
# Example, for list: 1 => 2 => 3 => 4 => 5;
# if head is the first node and n = 2, the function returns node with value 4


# PSEUDOCODE
# We return nil if the input head is nil, or if input n < 1, or if the size of linked list starting at head is less than input n.
# We create 2 pointers, result_pointer and front_pointer, and we initialize them to the input head.
# We advance front_pointer by n-1 nodes; this means that the 2 pointers will be separated by n nodes.
# Next, we advance both pointers at a rate of 1 until front_pointer reaches the last node (i.e front_pointer's next node is nil);
# When front_pointer reaches the last node of the list, this means that result_pointer is at the nth node from the last node since the 2 nodes are separated by a distance of n.
# We return result_pointer

def nth_node_from_end(head, n)
  return nil if !head || n < 1
  # Error check: return nil if the size of the linked list is < n
  return nil if LinkedList.new(head).length < n # Return nil if n is greater than the length of the linked list

  result_pointer = head
  front_pointer = head

  count = 0
  # Advance front_pointer by n-1 nodes
  while count < n - 1
    front_pointer = front_pointer.next_node # go to next node
    count += 1 # increment count
  end

  # Now move both pointers until front_pointer reaches the last node
  while front_pointer.next_node != nil
    result_pointer = result_pointer.next_node
    front_pointer = front_pointer.next_node
  end
  result_pointer
end

# TEST DRIVE
e = Node.new(5, nil)
d = Node.new(4, e)
c = Node.new(3, d)
b = Node.new(2, c)
a = Node.new(1, b)

# p nth_node_from_end(a, 2) # will return node d (of value 4)
p nth_node_from_end(a, 2).value == 4
p nth_node_from_end(a, 5).value == 1 # returns node a (of value 1)
p nth_node_from_end(a, 6) == nil # 6 is > the length of the list, 5
p nth_node_from_end(d, 3) == nil # 3 > 2 (length of list starting at d)
