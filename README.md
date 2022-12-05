[![Build Status](https://travis-ci.org/epot/Gifter.svg?branch=master)](https://travis-ci.org/epot/Gifter)

# Gifter

Play! application to deal with Gifts !

This has been developed with Play! 2.6 on the backend side, and Angular 5 on the frontend side.
This application was mainly created to have fun with the Play! Framework, but is used every year in my family to
organize for the Chrismas Gifts.

It is based on Postgres and Slick to handle the persistent storage, and Silhouette for authentication.

This is deployed with heroku here: https://giftyou.herokuapp.com/

# Start locally

You need to run the frontend first:

```
cd app-ui
npm install
npm start
```

And then the backend. You need to install postgres and install a `gifter3` database locally. For instance on macos:

```
brew install postgresql
brew services start postgresql
psql postgres

psql (14.0)
Type "help" for help.

postgres=# CREATE DATABASE gifter3;
CREATE DATABASE
```

And then:

```
export DD_AGENT_HOST=localhost
sbt
[Gifter] $ run
```

Open `localhost:9000` in your browser.

You will need to export silhouette env var to use oauth2. For instance for google:

```
export GOOGLE_CLIENT_ID=<get if from your google developer console>
export GOOGLE_CLIENT_SECRET=<get if from your google developer console>
```
