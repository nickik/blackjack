(ns blackjack.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [blackjack.game :as game]
            [clojure.spec :as s]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response (format "Go to <a href='%s'> %s </a> to see a test situation, complete the situation and send results with POST to %s"
                              (route/url-for ::situation-page)
                              (route/url-for ::situation-page)
                              (route/url-for ::situation-page)) ))

(defn situation-page
  [request]
  (let [situation (game/generate-random-situation)]
    (ring-resp/response (str (str "<p> Dealer:<br> " (::game/dealer-hand situation) "</p>" "<br><br><p> Player: <br> " (::game/player-hand situation) "</p>")
                             (apply str (map #(str "<button> " % "</button><br>") (vec (s/describe ::game/move)))))
                        )))




(defn situation-post-page
  [request]
  (let [solution (:edn-params request)]

    (println "stuff: ")
    (clojure.pprint/pprint solution)

    (if (s/valid? ::game/resolved-situation solution)
      (if (= (game/correct-action solution)
             (::game/move solution))
        (ring-resp/response "ok")
        (ring-resp/response "incorrect"))
      (ring-resp/response "unvalid input"))))


;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) http/html-body])

;; Tabular routes
#_(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/about" :get (conj common-interceptors `about-page)]})

;; Map-based routes
(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
                   :get home-page
                   "/about" {:get about-page}
                   "/situation" {:get situation-page
                                 :post situation-post-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])


;; Consumed by blackjack.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ::http/type :jetty
              ;;::http/host "localhost"
              ::http/port 8080
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false}})

