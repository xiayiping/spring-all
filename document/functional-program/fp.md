

## Functional Programme

### Monoid

- closure (set of values + combination operator)  : useful for collection folder
- Associativity   : useful for parallel
- Identity Element    : first element in collection folder | Empty element

The combination of Monoid is also a Monoid.

### Monad

#### Effect
List<_>  Option<_>  Resunt<>  Task<>  Async<>  etc......

### Design pattern

- map: map a non-monoid to a Monoid
- map: map inside Effect. Sample:
```javascript
    let add42ToEach = List.map (+42)
    add42ToEach [1,2,3]
```



