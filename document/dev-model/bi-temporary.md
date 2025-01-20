# 双时间快照

系统中经常会遇到需要请求某个过去特定时间状态的需求。将业务时间戳加入单行记录，并存入audit表是一个普遍的解决方案。

比如： 某个id为1的记录 ， 在时间 $ t1 $ ，字段 $A$ 从 'a' 改为 'b'，那么在audit表中，这个id为1的记录就有两条记录
- $id=1$ , $t0$时 $A='a'$
- $id=1$ , $t1$时 $A='b'$
- 第一条快照 $A='a'$ 的生效时间即为 $[t0, t1)$， 即如果要查询任意 $ t \in [t0, t1) $ 业务时间的快照时，返回第一条记录
- 同理如果查询任意 $ t \in [t1, +\infty) $业务时间的快照时，返回第二条记录

业务时间戳并不一定等于真实修改的时间，比如按某个业务规则，记录状态的更新时间应为在当天凌晨，但由于更新并非瞬时发生，真实更改时间往往不在凌晨。

因此除了表示业务生效时间（称为 *effective_time*），再添加一个时间，表示存入数据库时间，称为 *transaction_time*

## 具体规则

1. `此文档描述某个ID的记录形成的所有快照的集合 S`
2. `对于任意 ` $e \in S $ `有如下属性`
   - `id` 记录的ID
   - `et_start` 业务生效开始时间 (include)
   - `et_end` 业务生效结束时间 (exclude)
   - `tt` 快照保存进数据库的时间
3. `对一个S ，`$\forall e1, e2 \in S , e1.id == e2.id $ `(对应第一大规则，快照集合是指同一个id的数据的所有快照)` 
4. `tt 严格递增`
5. $ \exists\ et_0, 是et\_start的下界， st:$
   1. $\forall e\in S, e.et\_start \geq\ et_0.\ and$
   2. $\exists\ e\in S\ where\ e.et\_start == et_0$
   3. $et_0$`唯一，（有下界必有下确界）`
   4. `(总有一个创世的业务开始时间)`
6. $\exists\ e\in S\ where\ e.et\_end \to +\infty$ `(存在最后一个版本，一直有效，即没有被新的effective_time覆盖)`
7. $S\ 中元素的 [et\_start, et\_end)$`构成一个 `$[et_0, +\infty)$`的全覆盖`
8. `S 中元素 的 `$tt$`必有一个下确界` $tt_0$ `(第一个存入数据库的快照)`
9. $\forall\ tt^{\prime} \geq\ tt_0$ `可以在S中找出一个子集 `$S^{\prime}$ `such that (st):`
   1. $\forall e^{\prime} \in\ S^{\prime}\ e^{\prime}.tt \leq\ tt^{\prime}$
   2. $S^{\prime}$ `中的元素的 `$[et\_start, et\_end)$`构成一个 `$[et_0, +\infty)$`的全覆盖`
   3. $\forall e_n \in\ S^{\prime}\ : \cap [e_n.et\_start, e_n.et\_end) \in\ \emptyset$ `(子集的et没有交集)`
   4. `对于`$e_r \in\ S_r = S - S^{\prime}, $ `不存在` $e^{\prime}.et \cap e_r.et \notin \emptyset\ and\ e_r.tt \gt e^{\prime}.tt$ `(最大tt原则)`
10. `对于给定tt , et， 可以从`$S^{\prime}$`中取出一个` $et \in\ e^{\prime}.et$，`至此，找出了给定et tt的唯一快照`$e^{\prime}$
