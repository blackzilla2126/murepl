(ns murepl.handler
  (:gen-class)
  (:require [[murepl.core   :as    core        ]
             [murepl.events :as    events      ]
             [murepl.routes :refer [api-routes]]

             [clojure.tools.nrepl.server :as nrsrv]

             [taoensso.timbre            :as log]

             [ring.adapter.jetty         :refer [run-jetty]      ]
             [ring.middleware.clj-params :refer [wrap-clj-params]]
             [ring.middleware.gzip       :refer [wrap-gzip]      ]

             [compojure.route            :refer [resources]]])
  (:import
   (org.webbitserver WebServers)))

(def app
  (-> api-routes
      (wrap-clj-params)
      (wrap-gzip)))

(defn -main [& args]
  (let [args     (apply array-map args)
        host     (or (get args ":host") "localhost")
        port     (Integer. (or (get args ":port") 8888))
        ws-port  (Integer. (or (get args ":ws-port") 8889))
        log-file (or (get args ":log-file") "/tmp/MUREPL.log")]

    (log/set-config! [:timestamp-pattern] "yyyy-MM-dd HH:mm:ss ZZ")
    (log/set-config! [:appenders :spit :enabled?] true)
    (log/set-config! [:shared-appender-config :spit-filename] log-file)

    (log/debug "STARTUP: creating game world")
    (core/init!)

    (log/debug "STARTUP: starting nrepl")
    (defonce nrepl (nrsrv/start-server :port 7888))

    (log/debug "STARTUP: starting jetty on" host "port" port)
    (run-jetty app {:port port :host host :join? false})

    (log/debug "STARTUP: starting webbit on localhost port" ws-port)
    (doto (WebServers/createWebServer ws-port)
      (.add "/socket" events/ws)
      (.start))))