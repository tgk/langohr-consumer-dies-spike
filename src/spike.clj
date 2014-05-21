(ns spike
  (:require [langohr.core          :as rmq]
            [langohr.channel       :as lch]
            [langohr.queue         :as lq]
            [langohr.consumers     :as lc]
            [langohr.basic         :as lb]))

(def exchange "")

(defn- ensure-queue
  [ch queue]
  (lq/declare ch queue :durable true :auto-delete false))

(defn enqueue
  [queue]
  (let [conn     (rmq/connect)
        ch       (lch/open conn)]
    (try (ensure-queue ch queue)
         (println "Putting messages on queue")
         (doseq [i (range 25)]
           (lb/publish ch exchange queue
                       (if (< (rand) 0.1) "fail" "succeed")
                       :content-type "text/plain"))
         (finally (rmq/close ch)
                  (rmq/close conn)))))

(defn rabbitmq-message-handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  (let [s (String. payload)]
    (println "Received" s)
    (when (= "fail" s)
      (println "Throwing exception")
      (throw (ex-info "Dummy exception" {:string s})))))

(defn listen
  [queue]
  (println "Creating listener")
  (let [conn (rmq/connect)
        ch   (doto (lch/open conn)
               ;; The problem might be the qos of 1, but I'd still
               ;; expect to be handled a new piece of work
               (lb/qos 1))]
    (ensure-queue ch queue)
    (lc/subscribe ch queue
                  (lc/ack-unless-exception rabbitmq-message-handler)
                  ;; Adding :auto-ack false does not change anything,
                  ;; but it has been tested
                  ;; :auto-ack false
                  )
    (fn []
      (rmq/close ch)
      (rmq/close conn))))


(let [queue "test-queue"
      stop-listen (listen queue)]
  (enqueue queue)
  (Thread/sleep 500)
  (stop-listen)
  (println "Stopping consumer"))
