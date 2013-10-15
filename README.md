This is your new Play 2.1 application
=====================================

This file will be packaged with your application, when using `play dist`.

Run with:

  play -DapplyEvolutions.default=true

Setup
=====

EC2 Instance
------------

Alestic Ubuntu AMI

```bash
sudo su -
apg-get mysql-server unzip openjdk-7-jre emacs
a2enmod proxy_http
service apache2 restart
emacs /etc/apache2/sites-available/shufld
a2ensite shufld
service apache2 reload
```

MySQL
-----

Server

cd /usr/local/mysql/bin
sudo su - mysql ./mysqld

### Client

```bash
mysql -u root -p --default-character-set=utf8
```

### Schema

```sql
create user 'shufld' identified by 'shufld';
create database 'shufld';
grant all on shufld.* to 'shufld';
use shufld;
```

See 1.sql for create table scripts.

Obsolete
========

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


