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
