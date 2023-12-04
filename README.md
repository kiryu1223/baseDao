# baseDao
java lambda to static expressionTree to SQL

基于java lambda表达式的orm框架，现仅支持mysql

需要jdk8

欢迎使用，点star，issue，谢谢喵

# maven
```xml
<dependency>
    <groupId>io.github.kiryu1223</groupId>
    <artifactId>baseDao</artifactId>
    <version>1.0.10</version>
</dependency>
```

# how to start
1.添加maven依赖,并且在共享构建过程VM选项中添加
>-Djps.track.ap.dependencies=false

2.添加配置类
```java
@Configuration
public class baseDaoConfig
{
    @Bean
    public BaseDao baseDao(DataSource dataSource)
    {
        return new BaseDao(dataSource, DataBase.Type.Mysql);
    }
}
```

3.使用idea连接数据库并右键生成持久化映射

4.~~为你需要使用类添加`@Resolve`或`@Dao`注解~~ 不再需要

```java


@SpringBootTest
class HelloDockerApplicationTests
{
    @Autowired
    BaseDao bd;

    @Test
    void contextLoads()
    {
        bd.query(BookInfo.class)
                .where(a -> 1 == 1)
                .select(a -> a)
                .toListAndThen(r -> System.out.println(r));

        //Sql: select a.* from `book_info` as a where ? = ? 
        //values: [1, 1]
    }
}
```

5.项目 启动！

## 查询(query)

>query中的sql拼接顺序为链式调用的顺序，以select为结尾，比如

```java
baseDao.query(user.class)
       .leftJoin(book.class).on((a,b) -> a.getId == b.getId)
       .where((a,b) -> a.getCode == 1669)
       .orderBy((a,b) -> b.getId)
       .take(50)
       .select((a,b)-> (MyType) new MyType(){{
                setCode(a.getCode);
                setName(b.getName);
        }});
```

>会变成

```sql
select a.code,b.name from user as a left join book as b on a.id = b.id where a.code = 1669 order by b.id limit 50 
```
-----
1.`query`

**返回查询pojo类所对应的表的行为，基本上等同于mybatis返回一个sqlsession**

```java
baseDao.query(user.class);
```
等同于 
``` sql
select a.* from user as a
```
-----
2.`select`

**设置select选择的数据库字段与java返回类型，可以选择三种返回方式**

- 直接返回查询的pojo类
```java
baseDao.query(user.class).select(a->a);
```
等同于 
``` sql
select a.* from user as a
```

- ~~返回单个数据库字段~~ 暂时不再支持
```java
```
等同于 
``` sql
```

- 返回自己定义的新对象
```java
baseDao.query(user.class)
        .select(a-> (MyType) new MyType(){{
            setId(a.getId);
            setName(a.getName);
        }});
```
等同于
```java
 select a.id,a.name from user as a
```
-----
3.`where`

**查询条件，可以与数据库相关也可以无关（理论上什么都可以写）**

```java
baseDao.query(user.class)
        .where(a->a.getName().contains("kiryu"));
```
等同于
```sql
select a.* from user as a where a.id like "%kiryu%"
```
-----
4.`innerJoin`,`leftJoin`,`rightJoin`,`fullJoin`

**选择连表**

```java
baseDao.query(user.class)
        .leftJoin(book.class)
        .select((a,b)->b);
```
等同于
```sql
select b.* from user as a left join book as b
```
-----
5.`on`

**连表时的on条件**

```java
baseDao.query(user.class)
        .leftJoin(book.class).on((a,b) -> a.getId == b.getId)
        .select((a,b)->b);
```
等同于
```sql
select b.* from user as a left join book as b on a.id = b.id
```
-----
6.`orderBy`,`descOrderBy`

**根据选择的字段在数据库排序**

```java
baseDao.query(user.class)
        .orderBy(a->a.getid());
```
等同于
```sql
select a.* from user as a order by a.id
```
-----
7.`take`,`skip`

**获取xx跳过xx，等同于mysql中的limit和offset**

```java
baseDao.query(user.class)
        .take(3)
        .skip(1);
```
等同于
```sql
select a.* from user as a limit 3 offset 1
```
-----

9.`toList`

**返回List结果集**

>默认为ArrayList

```java
List<User> res = baseDao.query(User.class).toList();
//单表情况下可以在无select的情况下直接toList，此时会返回查询的pojo类的集合

List<User> res = baseDao.query(User.class).select(a->a).toList();
//select a.* from user as a

List<Integer> res = baseDao.query(User.class).select(a->a.getId).toList();
//select a.id from user as a

List<MyType> res = baseDao.query(User.class)
                            .where(a -> a.getId == 5)
                            .select(a -> (MyType) new MyType(){{
                                    setId(a.getId);
                                    setName(a.getName);
                            }})
                            .toList();
//select a.id,a.name from user as a where a.id = 5
```
-----
10.`toMap`

**返回Map结果集**

>默认为HashMap

```java
Map<Integer,User> res = baseDao.query(User.class).toMap(k -> k.getId()); //参数为一个表达式时，对返回的集合进行遍历获取MapKey
//select a.* from user as a

Map<Integer,String> res = baseDao.query(User.class).toMap(k -> k.getId(),v -> v.getName()); //参数为两个个表达式时，对返回的集合进行遍历同时获取MapKey和MapValue
//select a.* from user as a
```
## 新增(save)

1.`save(obj)`

对一个pojo对象的除@Id注解字段外的非null字段插入数据库

2.`save(objList)`

pojo对象列表的批量插入，条件同上

## 更新(update)

todo

## 删除(delete)

todo

