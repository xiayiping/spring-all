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
4. `对每一个插入的audit , tt 严格递增 （即有下确界）`
5. $ \exists\ et_0, 是et\_start的下界， st:$
   1. $\forall e\in S, e.et\_start \geq\ et_0.\ and$
   2. $\exists\ e\in S\ where\ e.et\_start == et_0$
   3. $et_0$`唯一，（有下界必有下确界）`
   4. `(即总有一个创世的业务开始时间)`
6. $\exists\ e\in S\ where\ e.et\_end \to +\infty$ `(存在最后一个版本，一直有效，即没有被新的effective_time覆盖)`
7. $S\ 中元素的 [et\_start, et\_end)$`构成一个 `$[et_0, +\infty)$`的全覆盖`
8. $\forall\ tt^{\prime} \geq\ tt_0$ `可以在S中找出一个子集 `$S^{\prime}$ `such that (st):`
   1. $\forall e^{\prime} \in\ S^{\prime}\ e^{\prime}.tt \leq\ tt^{\prime}$
   2. $\exists 下界 et_0^{\prime},\ S^{\prime}$ `中的元素的 `$[et\_start, et\_end)$`构成一个 `$[et_0^{\prime}, +\infty)$`的全覆盖`
   3. $\forall e_n \in\ S^{\prime}\ : \cap [e_n.et\_start, e_n.et\_end) \in\ \emptyset$ `(子集的et没有交集)`
   4. `对于`$e_r \in\ S_r = S - S^{\prime}, $ `不存在` $e^{\prime}.et \cap e_r.et \notin \emptyset\ and\ e_r.tt \gt e^{\prime}.tt$ `(最大tt原则)`
9. `对于给定tt >= tt0 , et >= et0， 可以从`$S^{\prime}$`中取出一个` $et \in\ e^{\prime}.et$，`至此，找出了给定et tt的唯一快照`$e^{\prime}$

## 实现方式

### 实现方式.1. 一条记录一个et 一个tt

在插入一个快照 audit 记录时，将记录的 et 字段加上，tt 为当前时间，插入数据库即可 

#### 查询操作和验证

1. 对 $record_1\ record_2$ ，当 $et_1 \leq et_2\ and\ tt_1 \leq\ tt_2$ 时:
   1. 当 $tt < tt_1\ or\ et < et_1$ 时，没有对应快照记录
   2. 当 $ tt_1 \leq tt < tt_2\ and\ et \geq et_1$ 时，总返回 $recored_1$
   3. 当 $ tt \geq tt_2\ and\ et_1 \leq et < et_2$ 时，总返回 $recored_1$
   4. 当 $ tt \geq tt_2\ and\ et \geq et_2$ 时，总返回 $recored_2$
   5. 满足规则1-9
2. 对 $record_1\ record_2$ ，当 $et_1 < et_2\ and\ tt_1 >\ tt_2$ 时， 优先最大tt 会返回recored1 ， 优先最大 et 会返回record2， 因此会有两种实现规则
   1. 当 $tt < tt_1 \ and\ \ et < et_2\ \ or\ \ et < et_1\ \ or \ \ tt < tt_2$时，没有快照记录
   2. 当 $tt \geq \ tt_1\ and\ \ et_1 \leq et < et_2$时，总返回 $recored_1$
   3. 当 $tt_2 \leq tt < \ tt_1\ and\ \ et \geq et_2$时，总返回 $recored_2$
   4. 当 $tt \geq tt_1\ and\ et \geq et_2$时， 就会有按tt 优先 或是 安 et 优先的两种方式
      1. 返回$record_1$ **（按tt最大优先）**
      2. 返回$record_2$ **（按et最大优先）** 
   5. 无论哪种方式，都满足规则1-9，因此开发项目应人为规定一个实现方式

#### 数据库查询

对于最大tt 优先时：

```sql
select *
from sample_table de
         inner join (select t2.id        as id,
                            max(t2.date) as et,
                            t1.tt        as tt
                     from sample_table t2
                              inner join (select id              as id,
                                                 max(createTime) as tt
                                          from sample_table stb
                                          where stb.createTime <= '20241201'
                                            and stb.date <= '20241201'
                                          group by stb.id) t1
                                         on t2.id = t1.id and t2.createTime = t1.tt and t2.date < '20241201'
                     group by t2.id, t1.tt) vttt
                    on de.createTime = vttt.tt and de.date = vttt.et and de.id = vttt.id
where 1 = 1
  and de.id = 1
  and de.tradingDate = 1
```

对于最大vt 优先时，

```sql
select *
from sample_table de
         inner join (select t2.id              as id,
                            t1.et              as et,
                            max(t2.createTime) as tt
                     from sample_table t2
                              inner join (select id        as id,
                                                 max(date) as et
                                          from sample_table stb
                                          where stb.date <= '20241201'
                                            and stb.createTime <= '20241201'
                                          group by stb.id) t1
                                         on t2.id = t1.id and t2.date = t1.et
                     group by t2.id, t1.tt) vttt
                    on de.createTime = vttt.tt and de.date = vttt.vt and de.id = vttt.id
where 1 = 1
  and de.id = 1
  and de.tradingDate = 1
```


### 实现方式.2. 一条记录一个et_start（闭）, 一个et_end （开），  一个tt

TBD...