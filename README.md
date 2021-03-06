  
# wren
Scala and Akka streams REST blog engine. 
This Blogging engine follows the programming paradigm: *Programming over Convention over Configuration*, meaning
that it provides only basic functionality, thus giving maximum flexibility for bloggers that are willed to do some programming on their own.

Live Demo: http://sandbox.sofen.de

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
## Get Posts by multiple Filters:
This is probably the only filter method you need:

Example:

```
http://localhost:9000/v1/nerds/posts/filter/date:0,90000/tags:matlab,holiday?compact=true&limit=2&offset=0&order=bydate&sort=asc
```

Simply add filters to the query: ```/filter-name:param,list```


| filter  | params | description |
| ------------- | ------------- | ------------ |
| date  | start,stop  | return all posts between start and stop timestamp |
| tags  | tag1,tag2 | return all posts that have at least one of the given tags |

TODO:
- filter by author
- filter by text (fuzzy)
- filter by geo bounding box


## Get Atom Feed:

Simple rss feed: 

```
http://localhost:9000/v1/nerds/feed
```


## Example Json Response for Post:

```
{
  "metadata": {
    "title": "Fat Jar mit SBT Bauen",
    "created": 1462912152,
    "tags": ["sbt", "scala"],
    "slug": "sbt_fatjar",
    "coverImage": "~/data/img.jpeg"
  },
  "post": {
    "content": "Bla bla bla"
    }
```

Of course, a list of posts will be a json ```[]``` array.

#Interface
See  https://github.com/sebsofen/wren-gui for a gui using WREN

# Server side custom Markdown syntax:

## include other post files:

```
[include file="path/to/file"]
```

A *~* in the file path will be replaced by the current post slug.


# Future:
Implementation of PostsRepository, CommentRepository, UserRepository. Each Blog can have own datasource for all three Repository types.

# Planned Features:

https://trello.com/b/44koPi3O