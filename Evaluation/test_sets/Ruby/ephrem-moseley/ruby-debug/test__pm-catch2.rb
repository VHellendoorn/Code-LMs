#!/usr/bin/env ruby
# Test Debugger.catchpoint 
def bar(arg)
  puts "bar begin"
  1/0 if arg
  raise ZeroDivisionError
  puts "bar end"
end

def foo
  puts "foo begin"
  yield 1
  puts "foo end"
rescue ZeroDivisionError
  puts "rescue"
end

def zero_div(arg)
  x = 5
  foo { |i| bar(i) }
  x + arg
rescue ZeroDivisionError
  "zero_div rescue"
end

puts zero_div(10)
puts "done"
