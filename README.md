# Buoyant Take Home Project

## Intro

Thank you for participating in Buoyant's take home project! This is intended to
familiarize you with Buoyant's technologies while simultaneously giving all of
us insight into how we work together.

## Environment Setup

This project requires a working Docker environment. Setting up this environment
is not part of the assignment, and we want to get you past this as quickly as
possible. You can set up Docker by following the instructions at
[docker.com](https://www.docker.com/), but please reach out immediately if you
hit any bumps.

A successfully set up environment means you can run the two commands in the
[Startup section of README-LINKERD-TCP.md](README-LINKERD-TCP.md#startup).

For more information about this demo, check out our
[linkerd-tcp blog post](https://blog.buoyant.io/2017/03/29/introducing-linkerd-tcp/).

## Project

Your assignment is to build an API service that interacts with our
[linkerd-tcp demo](https://github.com/BuoyantIO/linkerd-examples/tree/master/linkerd-tcp).

You are an owner of this repository, and can push branches and commits to it at
will. For the sake of the assignment, we recommend developing each part of the
project on a branch, and submitting pull requests to merge that branch back into
master.

The project is divided into two parts. The first part of the project is to
create your service and implement the health check endpoint described in Part I
below. When the endpoint is ready, please submit a pull request before
proceeding to the second part. The second part of the project is to implement
the traffic shifting endpoint described in Part II below.

### Part I: Health Check endpoint

Implement an endpoint that returns information about the health of the demo:

#### API
`GET /health`

#### Successful response
HTTP Status code: 200
```json
{
  "linkerd": "up",
  "namerd": "up",
  "linkerd-tcp": "down",
  "linkerd-viz": "up"
}
```

#### Description

The return data should indicate `up` if the service is healthy, otherwise
`down`.

Example usage:

```bash
curl localhost:1234/health
{"linkerd":"up","namerd":"up","linkerd-tcp":"down","linkerd-viz":"up"}
```

Your endpoint should gather health information from four of the services running
inside the demo. The following examples use curl to demonstrate the request and
response patterns that you need to implement in order to health check all four
services:

```bash
# linkerd
curl $DOCKER_IP:9990/admin/ping
pong

# namerd
curl $DOCKER_IP:9991/admin/ping
pong

# linkerd-tcp
curl -s -o /dev/null -w "%{http_code}" $DOCKER_IP:9992/metrics
200

# linkerd-viz
curl -s -o /dev/null -w "%{http_code}" $DOCKER_IP:3000
200
```

When you've completed this endpoint, please submit a pull request with your
change and we will review it promptly.

### Part II: Traffic Shifting endpoint

Implement a second endpoint that shifts traffic between two redis clusters:

#### API
`PUT /shift/:N`

#### Successful response
HTTP Status code: 204

#### Description

`:N` is the percentage of traffic shifted. When `N`=`0`, all traffic goes to
`redis1`. When `N`=`100`, all traffic goes to `redis2`.

Example usage:

```bash
curl -s -o /dev/null -w "%{http_code}" -X PUT localhost:1234/shift/25
204
```

When the demo first boots, all traffic points to `redis1`. You can use the
[namerd http api](https://linkerd.io/config/1.0.0/namerd/index.html#http-controller)
to shift traffic between `redis1` and `redis2`. The following example uses curl
to demonstrate a successful request and response to the namerd api that shifts
25% of traffic to `redis2`:

```bash
curl -s -o /dev/null -w "%{http_code}" \
  -X PUT -d"/svc => /#/io.l5d.fs; /svc/redis=> 75*/svc/redis1 & 25*/svc/redis2;" \
  -H "Content-Type: application/dtab" \
  $DOCKER_IP:4180/api/1/dtabs/default
204
```

This shifting is accomplished by updating namerd's stored routing rules, called
Delegation tables (dtabs for short). For more information about dtabs, see the
[dtab documentation](https://linkerd.io/in-depth/dtabs/).

Here are some examples of dtabs that can be used to shift traffic in the demo:

```bash
# all traffic shifted to redis1
/svc       => /#/io.l5d.fs;
/svc/redis => 100*/svc/redis1 & 0*/svc/redis2;

# 25% of traffic shifted to redis2
/svc       => /#/io.l5d.fs;
/svc/redis => 75*/svc/redis1 & 25*/svc/redis2;

# 100% of traffic shifted to redis2
/svc       => /#/io.l5d.fs;
/svc/redis => 0*/svc/redis1 & 100*/svc/redis2;
```

When you've completed this endpoint, please submit a second pull request with
your change. This pull request will mark the completion of your project.

### Part III: Optional additional features

If you've completed Parts I and II and still feel like adding additional
features to your API service, go for it! Maybe the service could use a web UI?
Or how about surfacing some stats about your endpoints in the Grafana dashboard?
Think about what features you would need to successfully run the service in
production, or ping us for other suggestions. Again, this part of the project is
completely optional, so don't feel pressured to tackle it.

## Ground Rules

1. Use any languages or technologies you like.

2. You'll have a full week to complete Parts I & II of the project, but we do
   not expect you to work full time on it. Not including setup, these two parts
   should require about 4-8 hours of work combined.

3. We expect you to submit a pull request for Part I of the project midway
   through the week. This will give you an opportunity to seek answers to any
   unresolved questions you might have, and it's also a good opportunity for us
   to gain insight into what you're building and offer feedback to help guide
   you toward successful completion.

4. Submit a pull request for Part II of your project by the end of the week.
   Include instructions in the pull request for running your service so that we
   can test it out.

5. If you need additional help during the week, just ask! You can reach us by
   email, or join the [linkerd Slack](https://slack.linkerd.io) and ask
   questions in the main room, or send anyone at Buoyant a private message.

Good luck!
