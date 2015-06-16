# Murepl Framework

Pronounced _murpull_. Rhymes with _purple_.

## Features

### secure user data

Passwords are stored in postgresql and access to clojure.jdbc is blocked via
clojail. User info, unlike world info, has a source of truth in postgresql (not
RAM).

### multiple access modes

* streaming _websockets, telnet_
* async _http_

### hackable

Gameworld can be modified by players.

### persistent

Gameworld and user information is periodically synced to disk. All commands by
all players are also logged.

### scripting / extension

Rich language of game functions / macros exposed. Possible to expand with new
functions. 

### multi tenant

Can define multiple gameworlds in a single running JVM.

### Per user namespaces

Users can define functions and vars that are persisted in a private namespace.

## Elements of State

All state (except transaction log) is stored in memory. Each collection of state
can be serialized on demand into persistent storage. Transaction log is written
to disk immediately.

* per-user namespaces
* user information
* item information
* room information
* temporal information (what elements are in what room)
* transaction log

## External (playing) API

This API consists of Clojure code that is serialized and eval'd on the server
side.

## External (low level) API

This API is a set of HTTP routes that allow someone to observe and mutate the
world through means other than Clojure. This API can be used, for example, to
build non-REPL UIs over a Murepl gameworld.

## Internal (setup, management) API

This API is for Gods who are setting up and managing gameworlds. It is a set of
Clojure functions and macros.

## Libraries and Tools

### (Trapperkeeper)

**Until its need is definite, I'm going to hold off on using TK.**

The various communication mechanisms are exposed as TK services. Each gameworld
is itself a service that encapsulates game state. A service can use multiple
communication mechanisms by opting into the ones it wants.

### PostgreSQL

murepl depends on PostgreSQL. It uses the JSON datatype to wholly preserve
snapshots of the various gameworld atoms. User information is stored encrypted
and decrypted client side. A pruning function deletes atom snapshots older than
N days.

### core.async

At its center, murepl uses core.async to handle incoming commands.

### http-kit

murepl uses http-kit as its HTTP/ws server. It provides the most
straightforward, pure clojure implementation of websocket serving. I also
considered Jetty and Webbit (rejected for being pure java interfaces) as well as
aleph (far too generalized/complicated).

### comidi

Murepl uses comidi to expose its HTTP/ws API.

### Clojail

Murepl prevents system-crashing commands using clojail.

### clojure.tools.namespace

The creation and management of namespaces is done via the built-in namespaces
library.

### Crypto

murepl uses openpgp.js for client side key generation / (en|de)cryption.
