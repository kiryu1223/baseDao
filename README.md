# baseDao
java lambda to static expressionTree to SQL

基于java lambda表达式的orm框架，现仅支持mysql

欢迎使用，点star，issue，谢谢喵

# maven
```xml
<dependency>
    <groupId>io.github.kiryu1223</groupId>
    <artifactId>baseDao</artifactId>
    <version>1.0.2</version>
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

4.为你需要使用类添加`@Resolve`或`@Dao`注解

```java
import static com.kiryu1223.baseDao.Dao.DBFunc.Sum;

@SpringBootTest
@Resolve
class HelloDockerApplicationTests
{
    @Autowired
    BaseDao baseDao;

    @Test
    void contextLoads()
    {
        baseDao.query(BookInfo.class)
                .where(a -> 1 == 1)
                .select(a -> Sum(a.getId()))
                .toListAndThen(r-> System.out.println(r));

        //Sql: select sum(a.id) from `book_info` as a where ? = ? 
        //values: [1, 1]
    }
}
```

5.项目 启动！

## 查询(query)

1.`query(arg)`

**返回查询pojo类所对应的表的行为，基本上等同于mybatis返回一个sqlsession**

```java
baseDao.query(user.class);
```
等同于 
``` sql
select a.* from user as a
```

2.`select(arg)`

**设置select选择的数据库字段与java返回类型，可以选择三种返回方式**

- 直接返回查询的pojo类
```java
baseDao.query(user.class).select(a->a);
```
等同于 
``` sql
select a.* from user as a
```

- 返回单个数据库字段
```java
baseDao.query(user.class).select(a->a.getId);
```
等同于 
``` sql
select a.id from user as a
```

- 返回自己定义的新对象
```java
baseDao.query(user.class)
        .select(a->new MyType(){{
            setId(a.getId);
            setName(a.getName);
        }});
```
等同于
```java
 select a.id,a.name from user as a
```

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

4.`innerJoin`,`leftJoin`,`rightJoin`,`fullJoin`

**选择连表**

```java
baseDao.query(user.class)
        .leftJoin(book.class)
        .select((a,b)->b);
```
等同于
```sql
select b.* from user as a leftjoin book as b
```

4.`on`

**连表时的on条件**

```java
baseDao.query(user.class)
        .leftJoin(book.class).on((a,b)->a.getId==b.getId)
        .select((a,b)->b);
```
等同于
```sql
select b.* from user as a leftjoin book as b on a.id = b.id
```
