Testing requirements
=============================

__Database setup__

This subproject tests require Postgres RDBMS to be available. Further we assume you have Pgsql installed locally on the 
same machine you're running tests at.

Enter psql:
`sudo -u postgres psql`

In psql prompt run the following queries:
```
create database lioqu;
create user lioqu with encrypted password 'passwd';
grant all privileges on database lioqu to lioqu;
```
Now you should be fine with `sbt test`