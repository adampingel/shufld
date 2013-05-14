This is your new Play 2.1 application
=====================================

This file will be packaged with your application, when using `play dist`.

Run with:

  play -DapplyEvolutions.default=true

Setup
=====

MySQL
-----

Server

  cd /usr/local/mysql/bin
  sudo su - mysql ./mysqld

Client

  mysql -u root -p --default-character-set=utf8

Schema

  See 1.sql for create table scripts.

  create user 'gameweb' identified by 'gameweb';
  grant all on gameweb.* to 'gameweb';

Zookeeper
---------

See http://www.findbestopensource.com/article-detail/zookeeper_cloud

```
cd /usr/local/zookeeper
sudo ./bin/zkServer.sh start

sudo ./bin/zkCli -server 127.0.0.1:2181
```

Creating mongo entries

```
create /mongodb Database
create /mongodb/mongodb1 127.0.0.1
ls /mongodb
get /mongodb/mongodb1 
```

MongoDB
-------

mongod


