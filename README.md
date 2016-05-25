  
# wren
Scala and Akka streams REST blog engine. 
This Blogging engine follows the programming paradigm: *Programming over Convention over Configuration*, meaning
that it provides only basic functionality, thus giving maximum flexibility for bloggers that are willed to do some programming on their own.

This Engine has multiple sub blogs support. Each Blog has to be specified in the ```application.conf``` : 

```
blogs = {
  nerds {
    posts = "/path/to/blg-nerds/"
  }
  trailmagic {
    posts = "/path/to/blg2/"
  }
}
```



# API-Documentation:

replace "nerds" with one of your blogs specified in ```application.conf```

## Get Posts as List:

```
http://localhost:9000/v1/nerds/posts?compact=true&limit=2&offset=0&order=bydate&sort=asc
```

all parameters are optional and have default values...

| Parameter  | Meaning |
| ------------- | ------------- |
| compact=true  | will trim post content at first empty newline (linux style).  |
| limit=10  | limit response to number of posts |
| offset=0 | offset response list by number |
| order=bydate | more to come |
| sort=asc | sort (asc) ascending or (desc) descending | 
| start=0 | timestamp setting lower bound for post creation date |
| stop=0 | timestamp setting upper bound for post creation date |


## Get Posts filtered by Tags:

```
http://localhost:9000/v1/nerds/posts/by-tags/tag1,tag2?compact=true&limit=2&offset=0&order=bydate&sort=asc
```

## Get Posts filtered by Search String:

```
http://localhost:9000/v1/nerds/posts/by-search/string to find in post?compact=true&limit=2&offset=0&order=bydate&sort=asc
```

## Example Json Response for Post:

```
{
  "metadata": {
    "title": "Fat Jar mit SBT Bauen",
    "created": 1462912152,
    "tags": ["sbt", "scala"],
    "slug": "sbt_fatjar"
  },
  "post": {
    "content": "Bla bla bla"
    }
```

Of course, a list of posts will be a json ```[]``` array.

#Interface
See  https://github.com/sebsofen/wren-gui for a gui using WREN

# Future:
Implementation of PostsRepository, CommentRepository, UserRepository. Each Blog can have own datasource for all three Repository types.

# Planned Features:

https://trello.com/b/44koPi3O