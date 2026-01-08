# Generator

`https://peps.python.org/pep-0008/`

```python

for i in range(2):
    pass

# the very raw version, but bad performance:
def my_range(n):
    i = 0
    result = []
    while i < n:
        result.append(i)
        i += 1
    return result

for i in my_range(2):
    pass

```

## Range

```python

class MyRangeI:
    def __init__(self, n):
        self.n = n

    def __iter__(self):
        return MyRangeIter(self.n)


class MyRangeIter:
    def __init__(self, n):
        self.n = n
        self.current = 0

    def __next__(self):
        if self.current >= self.n:
            raise StopIteration()
        result = self.current
        self.current += 1
        return result


for i in MyRangeI(2):
    pass

```

## Yield

```python

def my_range_y(n):
    i = 0
    while i < n:
        yield i
        i += 1

range_r = my_range_y(2)
# below will print 'class generator'
print(type(range_r))

# can :
it = range_r.__iter__()

# so far the body of my_range_y is not run
# run the body only when it.__next__() is called
# until the yield is hit and return the yield value

for i in my_range_y(3):
    print(i)

```